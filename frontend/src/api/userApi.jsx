import axios from "axios";

export const getAllUsers = async () => {
  const res = await axios.post("/api", form, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });

  const filtered = res.data;

  return filtered;
};
