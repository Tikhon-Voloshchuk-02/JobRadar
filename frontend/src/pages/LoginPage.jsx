import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { FiEye, FiEyeOff, FiMail, FiLock } from "react-icons/fi";

import { loginRequest, resendVerificationEmail } from "../api/api";
import { saveToken } from "../auth/auth";

import { GoogleLogin } from "@react-oauth/google";

import "./LoginPage.css";

function LoginPage() {
  const navigate = useNavigate();
  const { t } = useTranslation();

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);

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

  async function handleGoogleLogin(credentialResponse) {
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
  }

  async function handleResendVerification() {
    setError("");
    setResendSuccess("");

    try {
      setLoading(true);
      const data = await resendVerificationEmail(email);
      setResendSuccess(data.message || t("auth.verification_email_sent"));
    } catch (err) {
      setError(err.message || t("errors.resend_verification_failed"));
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="login-page">
      <div className="login-card">
        <div className="login-logo">JR</div>

        <h1>Welcome back to JobRadar</h1>
        <p className="login-subtitle">
          Sign in to continue tracking applications, emails and CV matching.
        </p>

        <form className="login-form" onSubmit={handleSubmit}>
          <label className="login-field">
            <FiMail className="login-field-icon" />
            <span>{t("auth.email")}</span>
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
          </label>

          <label className="login-field">
            <FiLock className="login-field-icon" />
            <span>{t("auth.password")}</span>
            <input
              type={showPassword ? "text" : "password"}
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />

            <button
              type="button"
              className="login-password-toggle"
              onClick={() => setShowPassword((prev) => !prev)}
              aria-label={showPassword ? "Hide password" : "Show password"}
            >
              {showPassword ? <FiEyeOff /> : <FiEye />}
            </button>
          </label>

          <button className="login-submit-button" type="submit" disabled={loading}>
            {loading ? t("loading") : t("auth.login")}
          </button>
        </form>

        <div className="login-divider">
          <span>or continue with</span>
        </div>

        <div className="google-login-wrapper">
          <GoogleLogin
            onSuccess={handleGoogleLogin}
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

        {resendSuccess && <p className="login-success">{resendSuccess}</p>}

        <p className="login-footer">
          {t("auth.no_account")}{" "}
          <Link to="/register">{t("auth.register")}</Link>
        </p>
      </div>
    </div>
  );
}

export default LoginPage;