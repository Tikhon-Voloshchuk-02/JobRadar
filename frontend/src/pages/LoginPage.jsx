import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { loginRequest } from "../api/api";
import { saveToken } from "../auth/auth";


function LoginPage(){
    const navigate = useNavigate();

    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);

    async function handleSubmit(e) {
        e.preventDefault();

        setError("");
        setLoading(true);

        try {
          const data = await loginRequest(email, password);

          saveToken(data.token);

          navigate("/dashboard");
        } catch (err) {
          setError(err.message || "Login failed");
        } finally {
          setLoading(false);
        }
    }

    return (
        <div>
          <h1>Login</h1>

          <form onSubmit={handleSubmit}>
            <div>
              <input
                type="email"
                placeholder="Email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
              />
            </div>

            <div style={{ marginTop: "10px" }}>
              <input
                type="password"
                placeholder="Password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
              />
            </div>

            <button type="submit" disabled={loading} style={{ marginTop: "10px" }}>
              {loading ? "Loading..." : "Login"}
            </button>
          </form>

          {error && <p style={{ color: "red" }}>{error}</p>}
        </div>
      );

    }

export default LoginPage;