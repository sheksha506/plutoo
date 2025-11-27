export const UserReducer = (state, action) => {
  switch (action.type) {
    case "LOADING":
      return { ...state, loading: true };

    case "SET_USERS":
      return { ...state, loading: false, users: action.payload };

    case "ERROR":
      return { ...state, loading: false, error: action.payload };

    default:
      return state;
  }
};
