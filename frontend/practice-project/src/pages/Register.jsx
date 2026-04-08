import React from "react";
import { useForm } from "react-hook-form";
import { registerUser } from "../api/auth.api";
import { Link } from "react-router-dom";

export default function Register() {
  const { register, handleSubmit, formState: { errors } } = useForm();

  const onSubmit = async (data) => {
    try {
      const res = await registerUser(data);
      console.log("Register success:", res);
      // optionally handle tokens from headers here
    } catch (err) {
      console.error("Register failed:", err);
    }
  };

  return (
    <div style={{ maxWidth: 400, margin: "auto" }}>
      <h2>Register</h2>
      <form onSubmit={handleSubmit(onSubmit)}>
        <div>
          <label>Name:</label>
          <input 
            type="text" 
            {...register("name", { required: "Name is required" })} 
          />
          {errors.name && <p>{errors.name.message}</p>}
        </div>
        <div>
          <label>Email:</label>
          <input 
            type="email" 
            {...register("email", { required: "Email is required" })} 
          />
          {errors.email && <p>{errors.email.message}</p>}
        </div>
        <div>
          <label>Password:</label>
          <input 
            type="password" 
            {...register("password", { required: "Password is required" })} 
          />
          {errors.password && <p>{errors.password.message}</p>}
        </div>
        <button type="submit">Register</button>
      </form>
      <p>
        Already have an account? <Link to="/login">Login</Link>
      </p>
    </div>
  );
}
