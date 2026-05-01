import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { api } from "../api/client";

const roles = ["ADMIN", "MEMBER"];

export default function AuthPage() {
  const navigate = useNavigate();
  const [mode, setMode] = useState("login");
  const [form, setForm] = useState({
    email: "",
    password: "",
    organizationName: "",
    role: "MEMBER"
  });
  const [error, setError] = useState("");

  const onChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const saveAuth = (data) => {
    localStorage.setItem("token", data.token);
    localStorage.setItem("user", JSON.stringify(data));
    navigate("/");
  };

  const submit = async (e) => {
    e.preventDefault();
    setError("");
    try {
      const endpoint = mode === "login" ? "/auth/login" : "/auth/signup";
      const payload =
        mode === "login"
          ? { email: form.email, password: form.password }
          : form;
      const { data } = await api.post(endpoint, payload);
      saveAuth(data);
    } catch (err) {
      setError(err?.response?.data?.message || "Authentication failed");
    }
  };

  return (
    <div className="auth-wrap">
      <div className="auth-card">
        <h1>FinishIT</h1>
        <p className="subtitle">Enterprise project and team operations platform</p>
        <div className="tab-row">
          <button className={mode === "login" ? "active" : ""} onClick={() => setMode("login")}>Login</button>
          <button className={mode === "signup" ? "active" : ""} onClick={() => setMode("signup")}>Signup</button>
        </div>
        <form onSubmit={submit}>
          <input name="email" placeholder="Work email" value={form.email} onChange={onChange} required />
          <input name="password" placeholder="Password" type="password" value={form.password} onChange={onChange} required />
          {mode === "signup" && (
            <>
              <input
                name="organizationName"
                placeholder="Organization name"
                value={form.organizationName}
                onChange={onChange}
                required
              />
              <select name="role" value={form.role} onChange={onChange}>
                {roles.map((r) => (
                  <option key={r} value={r}>
                    {r}
                  </option>
                ))}
              </select>
            </>
          )}
          <button type="submit" className="primary-btn">{mode === "login" ? "Login" : "Create account"}</button>
        </form>
        {error && <p className="error">{error}</p>}
      </div>
    </div>
  );
}
