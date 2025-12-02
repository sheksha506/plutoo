import axios from "axios";

export const getAllUsers = async () => {
  const res = await axios.get("/api", form);

  const filtered = res.data;

  return filtered;
};
