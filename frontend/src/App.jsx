import { Route, Routes } from "react-router-dom";
import Singup from "./auth/Singup";
import Login from "./auth/Login";
import Main from "./pages/Main";

import { UserProvider } from "./context/user/userContext"; // ⭐ import provider

function App() {
  return (
    <UserProvider>
      {" "}
      {/* ⭐ wrap entire app */}
      <Routes>
        <Route path="/signup" element={<Singup />} />
        <Route path="/" element={<Login />} />
        <Route path="/main" element={<Main />} />
      </Routes>
    </UserProvider>
  );
}

export default App;
