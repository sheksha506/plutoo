import axios from "axios";
import api from "../apiClient";

export const getAllUsers = async () => {
  const res = await api.get("/api");

  const filtered = res.data;

  return filtered;
};
