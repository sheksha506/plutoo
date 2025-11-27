import { createContext, useContext, useReducer } from "react";
import { UserReducer } from "./userReducer.jsx";
import { getAllUsers } from "../../api/userApi.jsx";

const userContext = createContext();

export const UserProvider = ({ children }) => {
  const initialState = {
    users: [],
    loading: false,
    error: null,
  };

  const [state, dispatch] = useReducer(UserReducer, initialState);

  const fetchAllUsers = async (token) => {
    try {
      dispatch({ type: "LOADING" });

      const filtered = await getAllUsers(token);

      dispatch({
        type: "SET_USERS",
        payload: filtered,
      });
    } catch (err) {
      dispatch({
        type: "ERROR",
        payload: err.message,
      });
    }
  };

  return (
    <userContext.Provider
      value={{
        users: state.users,
        loading: state.loading,
        error: state.error,
        fetchAllUsers,
      }}
    >
      {children}
    </userContext.Provider>
  );
};

export const useUser = () => useContext(userContext);
