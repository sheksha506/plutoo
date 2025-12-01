import React, {
  useEffect,
  useRef,
  useState,
  useCallback,
  useMemo,
} from "react";
import { useUser } from "../context/user/userContext";
import { jwtDecode } from "jwt-decode";
import axios from "axios";

/* ---------------------- MEMO COMPONENTS ---------------------- */

const UserRow = React.memo(
  ({
    u,
    followStatuses,
    sendFollowRequest,
    unfollowUser,
    onSelect,
    activeTab,
    unreadCount,
  }) => {
    const status = followStatuses[u.id] || "NONE";
    const isFriend = status === "ACCEPTED";

    const showUnreadBadge = activeTab === "friends" && isFriend;

    return (
      <div
        onClick={() => {
          if (activeTab === "friends") onSelect(u);
        }}
        className="flex items-center justify-between border rounded w-full bg-white/20 px-4 py-2 my-2 cursor-pointer 
                   hover:bg-white/10 text-sm md:text-base"
      >
        <span className="truncate">{u.username}</span>

        {showUnreadBadge ? (
          unreadCount > 0 && (
            <span className="ml-2 px-3 py-1 rounded-full bg-green-500 text-[11px] md:text-sm text-white">
              {unreadCount}
            </span>
          )
        ) : (
          <button
            onClick={(e) => {
              e.stopPropagation();
              if (status === "PENDING" || status === "ACCEPTED")
                unfollowUser(u.id);
              else sendFollowRequest(u.id);
            }}
            className="border rounded px-3 py-1 bg-blue-500 hover:bg-blue-300 text-[11px] md:text-sm"
          >
            {status === "PENDING" || status === "ACCEPTED"
              ? "UNFOLLOW"
              : "FOLLOW"}
          </button>
        )}
      </div>
    );
  }
);

const RequestRow = React.memo(({ req, acceptRequest, rejectRequest }) => (
  <div className="flex items-center justify-between border bg-white/20 px-4 py-2 my-2 rounded text-sm md:text-base">
    <span className="font-semibold truncate">{req.username}</span>
    <div className="flex gap-2">
      <button
        onClick={() => acceptRequest(req.senderId)}
        className="px-3 py-1 bg-green-600 rounded text-[11px] md:text-sm"
      >
        Accept
      </button>
      <button
        onClick={() => rejectRequest(req.senderId)}
        className="px-3 py-1 bg-red-600 rounded text-[11px] md:text-sm"
      >
        Reject
      </button>
    </div>
  </div>
));

/* ---------------------- MAIN COMPONENT ---------------------- */

const Main = () => {
  const token = localStorage.getItem("token");
  const { users, fetchAllUsers } = useUser();

  const authInfo = useMemo(() => {
    if (!token) return { email: null, username: null, userId: null };
    const decoded = jwtDecode(token);
    return {
      email: decoded.sub,
      username: decoded.username,
      userId: decoded.userId,
    };
  }, [token]);

  const { email, username, userId } = authInfo;

  const [search, setSearch] = useState("");
  const [user, setUser] = useState(null);
  const [message, setMessage] = useState("");
  const [messages, setMessages] = useState([]);
  const [followStatuses, setFollowStatuses] = useState({});
  const [activeTab, setActiveTab] = useState("friends");
  const [pendingRequests, setPendingRequests] = useState([]);
  const [statusesLoaded, setStatusesLoaded] = useState(false);
  const [readMessageIds, setReadMessageIds] = useState({});
  const [nearbyUsers, setNearbyUsers] = useState([]);
  const [userLocation, setUserLocation] = useState(null);

  const wsRef = useRef(null);
  const chatContainerRef = useRef(null);
  const clickTrackerRef = useRef({ lastTs: 0, count: 0 });
  const swipeRef = useRef({ startX: 0, startY: 0, isSwiping: false });

  /* ---------------------- FETCH USERS ---------------------- */
  useEffect(() => {
    fetchAllUsers();
  }, []);

  /* ---------------------- LOCATION UPDATES ---------------------- */
  useEffect(() => {
    if (!token || !username) return;
    if ("geolocation" in navigator) {
      const updateLocation = async () => {
        navigator.geolocation.getCurrentPosition(
          async (pos) => {
            const coords = {
              lat: pos.coords.latitude,
              lon: pos.coords.longitude,
            };
            setUserLocation(coords);
            try {
              await axios.post(
                "http://localhost:8083/api/nearby/updateLocation",
                { username, lat: coords.lat, lon: coords.lon },
                { headers: { Authorization: `Bearer ${token}` } }
              );
            } catch (err) {
              console.error("Error updating location:", err);
            }
          },
          (err) => console.error("Geolocation error:", err),
          { enableHighAccuracy: true }
        );
      };
      updateLocation();
      const intervalId = setInterval(updateLocation, 10000);
      return () => clearInterval(intervalId);
    }
  }, [token, username]);

  /* ---------------------- INITIAL MESSAGES ---------------------- */
  useEffect(() => {
    if (!token || !email) return;
    const fetchInitialMessages = async () => {
      try {
        const res = await axios.get("http://localhost:8081/api/messages", {
          headers: { Authorization: `Bearer ${token}` },
        });
        const data = res.data || [];
        setMessages(data);
        const initialRead = {};
        data.forEach((m) => {
          if (m.id) initialRead[m.id] = true;
        });
        setReadMessageIds(initialRead);
      } catch (err) {
        console.error("Error fetching messages:", err);
      }
    };
    fetchInitialMessages();
  }, [token, email]);

  /* ---------------------- WEBSOCKET ---------------------- */
  useEffect(() => {
    if (!token) return;
    const protocol = window.location.protocol === "https:" ? "wss" : "ws";
    const wsUrl = `${protocol}://localhost:8081/ws?token=${token}`;
    const ws = new WebSocket(wsUrl);
    wsRef.current = ws;

    ws.onopen = () => console.log("✅ WebSocket connected");
    ws.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data);
        if (data?.type === "DELETE_BOTH" && data.messageId) {
          setMessages((prev) => prev.filter((m) => m.id !== data.messageId));
        } else {
          setMessages((prev) => [...prev, data]);
        }
      } catch (e) {
        console.error("Error parsing WS message:", e);
      }
    };
    ws.onerror = (err) => console.error("WebSocket error:", err);
    ws.onclose = () => console.log("❌ WebSocket disconnected");

    return () => ws.close();
  }, [token]);

  /* ---------------------- FOLLOW STATUSES ---------------------- */
  const loadStatuses = useCallback(async () => {
    if (!users || users.length === 0 || !token) return;
    try {
      const receiverIds = users.map((u) => u.id);
      const res = await axios.post(
        "http://localhost:8082/api/follow/statuses",
        receiverIds,
        { headers: { Authorization: `Bearer ${token}` } }
      );
      const map = {};
      res.data.forEach((dto) => {
        if (dto.followStatus === "ACCEPTED") {
          map[dto.receiverId] = "ACCEPTED";
          map[dto.senderId] = "ACCEPTED";
        } else map[dto.receiverId] = dto.followStatus;
      });
      setFollowStatuses(map);
      setStatusesLoaded(true);
    } catch (err) {
      console.error("Error fetching follow statuses:", err);
    }
  }, [users, token]);

  useEffect(() => {
    loadStatuses();
  }, [loadStatuses]);

  const sendFollowRequest = useCallback(
    async (receiverId) => {
      try {
        const res = await axios.post(
          "http://localhost:8082/api/follow/request",
          { username, receiverId },
          { headers: { Authorization: `Bearer ${token}` } }
        );
        setFollowStatuses((prev) => ({
          ...prev,
          [receiverId]: res.data.followStatus,
        }));
      } catch (err) {
        console.error("Error sending follow request:", err);
      }
    },
    [username, token]
  );

  const unfollowUser = useCallback(
    async (receiverId) => {
      try {
        await axios.delete(`http://localhost:8082/api/follow/${receiverId}`, {
          headers: { Authorization: `Bearer ${token}` },
        });
        setFollowStatuses((prev) => ({ ...prev, [receiverId]: "NONE" }));
      } catch (err) {
        console.error("Error unfollowing user:", err);
      }
    },
    [token]
  );

  const fetchPending = useCallback(async () => {
    try {
      const res = await axios.get(
        "http://localhost:8082/api/follow/pendingRequest",
        { headers: { Authorization: `Bearer ${token}` } }
      );
      setPendingRequests(res.data);
    } catch (err) {
      console.error("Error loading pending:", err);
    }
  }, [token]);

  const acceptRequest = useCallback(
    async (senderId) => {
      try {
        await axios.post(
          "http://localhost:8082/api/follow/accept",
          { senderId },
          { headers: { Authorization: `Bearer ${token}` } }
        );
        setPendingRequests((prev) =>
          prev.filter((p) => p.senderId !== senderId)
        );
        setFollowStatuses((prev) => ({ ...prev, [senderId]: "ACCEPTED" }));
        loadStatuses();
      } catch (err) {
        console.error("Accept error:", err);
      }
    },
    [token, loadStatuses]
  );

  const rejectRequest = useCallback(
    async (senderId) => {
      try {
        await axios.post(
          "http://localhost:8082/api/follow/reject",
          { senderId },
          { headers: { Authorization: `Bearer ${token}` } }
        );
        setPendingRequests((prev) =>
          prev.filter((p) => p.senderId !== senderId)
        );
      } catch (err) {
        console.error("Reject error:", err);
      }
    },
    [token]
  );

  /* ---------------------- FETCH NEARBY USERS ---------------------- */
  const fetchNearby = useCallback(async () => {
    if (!userLocation) return;
    try {
      const res = await axios.get("http://localhost:8083/api/nearby", {
        params: { lat: userLocation.lat, lon: userLocation.lon, distanceKm: 2 },
        headers: { Authorization: `Bearer ${token}` },
      });
      console.log(res.data);
      setNearbyUsers(
        res.data
          .filter((u) => u.username !== username)
          .map((u) => {
            const full = users.find((x) => x.username === u.username);
            return {
              id: full?.id || u.userId,
              username: u.username,
              email: full?.email || null,
              distanceInKm: u.distanceInKm,
            };
          })
      );
    } catch (err) {
      console.error("Error fetching nearby users:", err);
    }
  }, [userLocation, token, username, users]);

  useEffect(() => {
    if (activeTab === "nearby") fetchNearby();
  }, [activeTab, fetchNearby]);

  /* ---------------------- CHAT HANDLERS ---------------------- */
  const handleMessages = useCallback(() => {
    if (
      !message.trim() ||
      !user?.email ||
      !wsRef.current ||
      wsRef.current.readyState !== WebSocket.OPEN
    )
      return;
    const outgoing = { receiver: user.email, content: message.trim() };
    wsRef.current.send(JSON.stringify(outgoing));
    setMessages((prev) => [
      ...prev,
      {
        ...outgoing,
        sender: email,
        timestamp: new Date().toISOString(),
        delivered: false,
        local: true,
      },
    ]);
    setMessage("");
  }, [message, user, email]);

  const deleteMessageForMe = useCallback(
    async (msg) => {
      setMessages((prev) =>
        prev.filter((m) => (msg.id ? m.id !== msg.id : m !== msg))
      );
      if (!msg.id || !token) return;
      try {
        await axios.delete(`http://localhost:8081/api/messages/${msg.id}/me`, {
          headers: { Authorization: `Bearer ${token}` },
        });
      } catch (err) {
        console.error("Error deleting message for me:", err);
      }
    },
    [token]
  );

  const deleteMessageForBoth = useCallback(
    async (msg) => {
      setMessages((prev) =>
        prev.filter((m) => (msg.id ? m.id !== msg.id : m !== msg))
      );
      if (!msg.id || !token) return;
      try {
        await axios.delete(
          `http://localhost:8081/api/messages/${msg.id}/both`,
          { headers: { Authorization: `Bearer ${token}` } }
        );
      } catch (err) {
        console.error("Error deleting message for both:", err);
      }
    },
    [token]
  );

  const handleBubbleClick = useCallback(
    (msg) => {
      const now = Date.now();
      const info = clickTrackerRef.current;
      if (now - info.lastTs < 350) info.count += 1;
      else info.count = 1;
      info.lastTs = now;
      if (info.count === 2) deleteMessageForMe(msg);
    },
    [deleteMessageForMe]
  );

  const handleTouchStart = useCallback((e) => {
    const touch = e.touches[0];
    swipeRef.current = {
      startX: touch.clientX,
      startY: touch.clientY,
      isSwiping: true,
    };
  }, []);

  const handleTouchMove = useCallback((e) => {
    if (!swipeRef.current.isSwiping) return;
    const touch = e.touches[0];
    const dx = touch.clientX - swipeRef.current.startX;
    const dy = touch.clientY - swipeRef.current.startY;
    if (Math.abs(dx) > Math.abs(dy) && Math.abs(dx) > 5) e.preventDefault();
    else if (Math.abs(dy) > Math.abs(dx)) swipeRef.current.isSwiping = false;
  }, []);

  const handleTouchEnd = useCallback(
    (e, msg) => {
      const swipe = swipeRef.current;
      if (!swipe.isSwiping) return;
      const touch = e.changedTouches[0];
      const dx = touch.clientX - swipe.startX;
      const dy = touch.clientY - swipe.startY;
      if (dx > 60 && Math.abs(dy) < 50) deleteMessageForBoth(msg);
      swipeRef.current.isSwiping = false;
    },
    [deleteMessageForBoth]
  );

  /* ---------------------- MARK MESSAGES READ ---------------------- */
  useEffect(() => {
    if (!user || !email) return;
    setReadMessageIds((prev) => {
      const updated = { ...prev };
      messages.forEach((m) => {
        if (m.sender === user.email && m.receiver === email && m.id)
          updated[m.id] = true;
      });
      return updated;
    });
  }, [user, messages, email]);

  /* ---------------------- UNREAD COUNTS ---------------------- */
  const unreadCountsByEmail = useMemo(() => {
    const counts = {};
    messages.forEach((m) => {
      if (m.receiver === email && m.id && !readMessageIds[m.id] && m.sender)
        counts[m.sender] = (counts[m.sender] || 0) + 1;
    });
    return counts;
  }, [messages, email, readMessageIds]);

  /* ---------------------- FILTERED USERS ---------------------- */
  const displayedUsers = useMemo(() => {
    if (!statusesLoaded || !users) return [];
    if (activeTab === "friends")
      return users.filter((u) => followStatuses[u.id] === "ACCEPTED");
    if (activeTab === "suggestions")
      return users.filter((u) => u.id !== userId);
    if (activeTab === "nearby") return nearbyUsers;
    return [];
  }, [activeTab, users, followStatuses, statusesLoaded, userId, nearbyUsers]);

  /* ---------------------- CURRENT CONVERSATION ---------------------- */
  const conversationMessages = useMemo(() => {
    if (!user || !user.email) return [];
    return messages.filter(
      (m) =>
        (m.sender === email && m.receiver === user.email) ||
        (m.sender === user.email && m.receiver === email)
    );
  }, [messages, user, email]);

  const firstUnreadIndex = useMemo(() => {
    if (!user || !email) return -1;
    for (let i = 0; i < conversationMessages.length; i++) {
      const m = conversationMessages[i];
      if (m.receiver === email && m.id && !readMessageIds[m.id]) return i;
    }
    return -1;
  }, [conversationMessages, user, email, readMessageIds]);

  useEffect(() => {
    if (!chatContainerRef.current) return;
    chatContainerRef.current.scrollTo({
      top: chatContainerRef.current.scrollHeight,
      behavior: "smooth",
    });
  }, [conversationMessages.length, user]);

  /* ---------------------- RENDER ---------------------- */
  return (
    <div className="h-screen flex flex-col lg:flex-row overflow-hidden">
      <div
        className={`w-full lg:w-[30%] flex flex-col h-full border-r border-gray-200 ${
          user ? "hidden lg:flex" : "flex"
        }`}
      >
        <h1 className="font-bold text-lg md:text-3xl p-2 m-2">Pluto</h1>
        <input
          type="text"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          placeholder="Search"
          className="w-full p-2 m-2 text-black bg-white/10 rounded text-xs md:text-base"
        />
        <div className="flex flex-row gap-1 m-2 overflow-x-auto no-scrollbar">
          {["friends", "nearby", "suggestions", "requests"].map((tab) => (
            <button
              key={tab}
              onClick={() => {
                setActiveTab(tab);
                if (tab === "requests") fetchPending();
                if (tab === "nearby") fetchNearby();
              }}
              className={`flex-shrink-0 py-1 rounded-xl border border-white/20 px-2 text-xs md:text-sm ${
                activeTab === tab
                  ? "bg-blue-500 text-white"
                  : "bg-white/10 text-black"
              }`}
            >
              {tab.charAt(0).toUpperCase() + tab.slice(1)}
            </button>
          ))}
        </div>
        <div className="flex flex-col flex-1 overflow-y-auto no-scrollbar">
          {activeTab !== "requests" &&
            displayedUsers
              .filter((u) =>
                u.username.toLowerCase().startsWith(search.toLowerCase())
              )
              .map((u) => {
                const friendEmail = u.email;
                const unreadCount = friendEmail
                  ? unreadCountsByEmail[friendEmail] || 0
                  : 0;
                return (
                  <UserRow
                    key={u.id}
                    u={u}
                    followStatuses={followStatuses}
                    sendFollowRequest={sendFollowRequest}
                    unfollowUser={unfollowUser}
                    onSelect={activeTab === "friends" ? setUser : () => {}}
                    activeTab={activeTab}
                    unreadCount={unreadCount}
                  />
                );
              })}
          {activeTab === "requests" &&
            pendingRequests.map((req) => (
              <RequestRow
                key={req.senderId}
                req={req}
                acceptRequest={acceptRequest}
                rejectRequest={rejectRequest}
              />
            ))}
        </div>
      </div>

      <div
        className={`flex flex-col flex-1 w-full h-full bg-gray-100 ${
          user ? "flex" : "hidden lg:flex"
        }`}
      >
        <div className="flex items-center p-2 border-b md:hidden">
          {user && (
            <button
              onClick={() => setUser(null)}
              className="px-2 py-1 bg-gray-200 rounded text-xs"
            >
              Back
            </button>
          )}
          <h1 className="font-bold text-sm ml-2">
            {user ? user.username : "Chat"}
          </h1>
        </div>
        <div className="hidden md:flex items-center p-3 border-b">
          <h1 className="font-bold text-xl md:text-2xl">
            {user ? user.username : "Select a friend to start chatting"}
          </h1>
        </div>

        <div
          ref={chatContainerRef}
          className="flex flex-col flex-1 overflow-y-auto p-2 space-y-1 no-scrollbar"
        >
          {!user ? (
            <div className="w-full h-full flex items-center justify-center text-gray-500 text-sm md:text-base">
              👈 Select a friend from the list to start chatting
            </div>
          ) : (
            conversationMessages.map((msg, idx) => {
              const isMe = msg.sender === email;
              const showUnreadSeparator = idx === firstUnreadIndex;
              return (
                <React.Fragment key={msg.id || idx}>
                  {showUnreadSeparator && (
                    <div className="w-full flex justify-center my-2">
                      <span className="px-3 py-1 text-[10px] md:text-xs bg-green-100 text-green-700 rounded-full">
                        Unread messages
                      </span>
                    </div>
                  )}
                  <div
                    onClick={() => handleBubbleClick(msg)}
                    onTouchStart={handleTouchStart}
                    onTouchMove={handleTouchMove}
                    onTouchEnd={(e) => handleTouchEnd(e, msg)}
                    className={`p-1 border rounded w-fit max-w-[75%] text-xs md:text-base touch-pan-y ${
                      isMe
                        ? "bg-blue-500 text-white self-end"
                        : "bg-white text-black self-start"
                    }`}
                  >
                    {msg.content}
                  </div>
                </React.Fragment>
              );
            })
          )}
        </div>

        {user && (
          <div className="flex items-center gap-2 p-2 border-t">
            <input
              value={message}
              onChange={(e) => setMessage(e.target.value)}
              onKeyDown={(e) => {
                if (e.key === "Enter" && !e.shiftKey) {
                  e.preventDefault();
                  handleMessages();
                }
              }}
              className="flex-1 h-10 md:h-11 px-3 border rounded text-xs md:text-sm"
              type="text"
              placeholder="Message"
            />
            <button
              onClick={handleMessages}
              className="h-10 md:h-11 px-4 rounded text-xs md:text-sm bg-blue-500 text-white flex items-center justify-center"
            >
              Send
            </button>
          </div>
        )}
      </div>
    </div>
  );
};

export default Main;
