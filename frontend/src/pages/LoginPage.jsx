import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { useTranslation } from "react-i18next";

import { loginRequest } from "../api/api";
import { saveToken } from "../auth/auth";

import { GoogleLogin } from "@react-oauth/google";

import "./LoginPage.css";

function LoginPage() {
  const navigate = useNavigate();
  const { t } = useTranslation();

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
      setError(err.message || t("errors.login_failed"));
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="login-page">
      <div className="login-card">
        <h1>{t("auth.login")}</h1>

        <form className="login-form" onSubmit={handleSubmit}>
          <input
            type="email"
            placeholder={t("auth.email")}
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
          />

          <input
            type="password"
            placeholder={t("auth.password")}
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />

          <button type="submit" disabled={loading}>
            {loading ? t("loading") : t("auth.login")}
          </button>
        </form>

        <div className="google-login-wrapper" style={{ marginTop: "20px" }}>
          <GoogleLogin
            onSuccess={async (credentialResponse) => {
              try {
                setError("");
                setLoading(true);

                const response = await fetch("/api/auth/google", {
                  method: "POST",
                  headers: {
                    "Content-Type": "application/json",
                  },
                  body: JSON.stringify({
                    idToken: credentialResponse.credential,
                  }),
                });

                if (!response.ok) {
                  throw new Error("Google login failed");
                }

                const data = await response.json();

                saveToken(data.token);
                navigate("/dashboard");
              } catch (err) {
                console.error(err);
                setError("Google login failed");
              } finally {
                setLoading(false);
              }
            }}
            onError={() => {
              console.log("Google Login Failed");
              setError("Google login failed");
            }}
          />
        </div>

        {error && <p className="login-error">{error}</p>}

        <p className="login-footer">
          {t("auth.no_account")}{" "}
          <Link to="/register">{t("auth.register")}</Link>
        </p>
      </div>
    </div>
  );
}
export default LoginPage;