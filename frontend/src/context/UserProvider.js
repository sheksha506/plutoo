import React, { useReducer, useEffect, useState } from "react";
import { UserContext } from "./UserContext";
import { userReducer, initialUserState } from "./UserReducer";
import { getAllUsers } from "../api/userApi";

const UserProvider = ({ children }) => {
  const [state, dispatch] = useReducer(userReducer, initialUserState);
  const [email, setEmail] = useState("");

  const token = localStorage.getItem("token");

  // Decode token to get logged in user
  

  // Get all users except logged-in user
  useEffect(() => {
    

    const fetchUsers = async () => {
      dispatch({ type: "LOADING" });

      try {
        const data = await getAllUsers();
        const filtered = data.filter((u) => u.email !== email);
        dispatch({ type: "SET_USERS", payload: filtered });
      } catch (err) {
        dispatch({ type: "ERROR", payload: "Unable to load users" });
      }
    };

    fetchUsers();
  }, [email]);

  return (
    <UserContext.Provider value={{ ...state }}>
      {children}
    </UserContext.Provider>
  );
};

export default UserProvider;
