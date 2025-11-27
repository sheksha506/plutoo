import React, { useReducer, useEffect, useState } from "react";
import { UserContext } from "./UserContext";
import { userReducer, initialUserState } from "./UserReducer";
import { getAllUsersApi } from "../api/userApi";

const UserProvider = ({ children }) => {
  const [state, dispatch] = useReducer(userReducer, initialUserState);
  const [email, setEmail] = useState("");

  const token = localStorage.getItem("token");

  // Decode token to get logged in user
  useEffect(() => {
    if (!token) return;

    const payload = token.split(".")[1];
    const decoded = JSON.parse(atob(payload));
    setEmail(decoded.sub);
  }, [token]);

  // Get all users except logged-in user
  useEffect(() => {
    if (!token || !email) return;

    const fetchUsers = async () => {
      dispatch({ type: "LOADING" });

      try {
        const data = await getAllUsersApi(token);
        const filtered = data.filter((u) => u.email !== email);
        dispatch({ type: "SET_USERS", payload: filtered });
      } catch (err) {
        dispatch({ type: "ERROR", payload: "Unable to load users" });
      }
    };

    fetchUsers();
  }, [email, token]);

  return (
    <UserContext.Provider value={{ ...state }}>
      {children}
    </UserContext.Provider>
  );
};

export default UserProvider;
