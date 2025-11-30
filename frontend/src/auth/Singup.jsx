import axios from "axios";
import React, { useState } from "react";
import { useNavigate } from "react-router-dom";

const Singup = () => {
  const navigate = useNavigate();

  const [form, setForm] = useState({
    username: "",
    email: "",
    password: "",
  });

  const handleChange = (e) => {
    setForm({
      ...form,
      [e.target.name]: e.target.value,
    });
  };

  const detailsSubmit = async (e) => {
    e.preventDefault();

    try {
      await axios.post(
  "http://backend-userservice:8080/api/signup",
  form
);

      navigate("/");
    } catch (error) {
      alert("Please enter the details correctly");
    }
  };

  return (
    <div className="h-screen bg-gray-100 flex flex-col md:flex-row items-center justify-center px-5 md:px-10">
      
      {/* Title & Subtitle */}
      <div className="w-full md:flex-1 mb-8 md:mb-0 text-center md:text-left">
        <h1 className="text-4xl md:text-5xl font-bold text-blue-600">
          NearBook
        </h1>
        <p className="text-lg md:text-xl mt-4 text-gray-700 hidden sm:block">
          Connect with nearby users and make new friends instantly.
        </p>
      </div>

      {/* Form */}
      <div className="w-full md:flex-1 flex justify-center">
        <form
          onSubmit={detailsSubmit} method="POST"
          className="flex flex-col w-full sm:w-4/5 md:w-[70%] bg-white shadow-lg p-6 sm:p-8 rounded-xl"
        >
          <h2 className="text-2xl font-semibold text-gray-800 text-center mb-6">
            Create Account
          </h2>

          <input
            name="username"
            className="p-3 mb-3 border rounded bg-gray-100 focus:outline-none focus:ring-2 focus:ring-blue-400 text-sm md:text-base"
            type="text"
            placeholder="Username"
            onChange={handleChange}
          />

          <input
            name="email"
            className="p-3 mb-3 border rounded bg-gray-100 focus:outline-none focus:ring-2 focus:ring-blue-400 text-sm md:text-base"
            type="email"
            placeholder="Email"
            onChange={handleChange}
          />

          <input
            name="password"
            className="p-3 mb-5 border rounded bg-gray-100 focus:outline-none focus:ring-2 focus:ring-blue-400 text-sm md:text-base"
            type="password"
            placeholder="Password"
            onChange={handleChange}
          />

          <button
            type="submit"
            className="bg-blue-600 hover:bg-blue-700 text-white font-semibold p-3 rounded-lg w-full transition-all text-sm md:text-base"
          >
            Sign Up
          </button>

          <p className="text-center text-sm text-black mt-4">
            Already have an account?{" "}
            <span className="text-blue-500 cursor-pointer hover:underline">
              Login
            </span>
          </p>
        </form>
      </div>
    </div>
  );
};

export default Singup;
