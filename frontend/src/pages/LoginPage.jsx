import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { useTranslation } from "react-i18next";

import { loginRequest, resendVerificationEmail } from "../api/api";
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

  const [resendSuccess, setResendSuccess] = useState("");
  const [showResendButton, setShowResendButton] = useState(false);

  function getLoginErrorMessage(err) {
    if (err.message === "Email not verified") {
      setShowResendButton(true);
      return t("auth.email_not_verified");
    }

    return err.message || t("errors.login_failed");
  }

  async function handleSubmit(e) {
    e.preventDefault();

    setError("");
    setResendSuccess("");
    setShowResendButton(false);
    setLoading(true);

    try {
      const data = await loginRequest(email, password);
      saveToken(data.token);
      navigate("/dashboard");
    } catch (err) {
      setError(getLoginErrorMessage(err));
    } finally {
      setLoading(false);
    }
  }

  async function handleResendVerification() {
    setError("");
    setResendSuccess("");

    try {
      setLoading(true);
      const data = await resendVerificationEmail(email);
      setResendSuccess(
        data.message || t("auth.verification_email_sent")
      );
    } catch (err) {
      setError(
        err.message || t("errors.resend_verification_failed")
      );
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
                  throw new Error(t("errors.google_login_failed"));
                }

                const data = await response.json();

                saveToken(data.token);
                navigate("/dashboard");
              } catch (err) {
                console.error(err);
                setError(err.message || t("errors.google_login_failed"));
              } finally {
                setLoading(false);
              }
            }}
            onError={() => {
              setError(t("errors.google_login_failed"));
            }}
          />
        </div>

        {error && <p className="login-error">{error}</p>}

        {showResendButton && (
          <button
            type="button"
            className="resend-verification-button"
            onClick={handleResendVerification}
            disabled={loading || !email}
          >
            {t("auth.resend_verification_email")}
          </button>
        )}

        {resendSuccess && (
          <p className="login-success">{resendSuccess}</p>
        )}

        <p className="login-footer">
          {t("auth.no_account")}{" "}
          <Link to="/register">{t("auth.register")}</Link>
        </p>
      </div>
    </div>
  );
}

export default LoginPage;