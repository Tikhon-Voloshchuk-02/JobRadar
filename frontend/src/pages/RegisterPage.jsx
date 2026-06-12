import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { FiEye, FiEyeOff, FiUser, FiMail, FiLock, FiCheck } from "react-icons/fi";

import { registerRequest } from "../api/api";

import "./RegisterPage.css";

function RegisterPage() {
  const navigate = useNavigate();
  const { t } = useTranslation();

  const [firstname, setFirstname] = useState("");
  const [lastname, setLastname] = useState("");
  const [email, setEmail] = useState("");

  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");

  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [loading, setLoading] = useState(false);

  function getPasswordStrength(password) {
      let score = 0;

    if (password.length >= 8) score++;
    if (/[A-Z]/.test(password)) score++;
    if (/[0-9]/.test(password)) score++;
    if (/[^A-Za-z0-9]/.test(password)) score++;

    if (score <= 1) return "weak";
    if (score <= 3) return "medium";
    return "strong";
  }

  const passwordStrength = getPasswordStrength(password);


  async function handleSubmit(e) {
    e.preventDefault();

    setError("");
    setSuccess("");

    if (password !== confirmPassword){
        setError("Passwords don't match");
        return;
    }

    setLoading(true);

    try {
      await registerRequest({
        firstname,
        lastname,
        email,
        password,
        confirmPassword,
      });

      setSuccess(t("auth.check_email"));

      // clean form
      setFirstname("");
      setLastname("");
      setEmail("");
      setPassword("");
      setConfirmPassword("");

      //  2 second redirect
      setTimeout(() => {
        navigate("/login");
      }, 2000);

    } catch (err) {
      setError(err.message || t("errors.registration_failed"));
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="register-page">
      <div className="register-card">
        <div className="register-header">
          <div className="register-logo">JR</div>

          <h1>Create your JobRadar account</h1>

          <p>
            Start tracking applications, emails and CV matching in one place.
          </p>
        </div>

        <form className="register-form" onSubmit={handleSubmit}>
          <div className="register-row">
            <div className="input-group">
              <FiUser className="input-icon" />

              <div className="field-content">
                <label>First name</label>
                <input
                  type="text"
                  value={firstname}
                  onChange={(e) => setFirstname(e.target.value)}
                  required
                />
              </div>
            </div>

            <div className="input-group">
              <FiUser className="input-icon" />

              <div className="field-content">
                <label>Last name</label>
                <input
                  type="text"
                  value={lastname}
                  onChange={(e) => setLastname(e.target.value)}
                  required
                />
              </div>
            </div>
          </div>

          <div className="input-group">
            <FiMail className="input-icon" />

            <div className="field-content">
              <label>Email address</label>
              <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
              />
            </div>

            {email && <FiCheck className="valid-icon" />}
          </div>

          <div className="input-group">
            <FiLock className="input-icon" />

            <div className="field-content">
              <label>Password</label>
              <input
                type={showPassword ? "text" : "password"}
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
              />
            </div>

            <button
              type="button"
              className="password-toggle"
              onClick={() => setShowPassword(!showPassword)}
            >
              {showPassword ? <FiEyeOff /> : <FiEye />}
            </button>
          </div>

          {password && (
            <div className="password-strength">
              <span>
                At least 8 characters with a mix of letters, numbers & symbols
              </span>

              <div className="strength-indicator">
                <strong className={`strength-text ${passwordStrength}`}>
                  {passwordStrength}
                </strong>

                <div className="strength-bars">
                  <span className="active"></span>
                  <span
                    className={
                      passwordStrength === "medium" || passwordStrength === "strong"
                        ? "active"
                        : ""
                    }
                  ></span>
                  <span
                    className={passwordStrength === "strong" ? "active" : ""}
                  ></span>
                </div>
              </div>
            </div>
          )}

          <div className="input-group">
            <FiLock className="input-icon" />

            <div className="field-content">
              <label>Confirm password</label>
              <input
                type={showConfirmPassword ? "text" : "password"}
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                required
              />
            </div>

            <button
              type="button"
              className="password-toggle"
              onClick={() =>
                setShowConfirmPassword(!showConfirmPassword)
              }
            >
              {showConfirmPassword ? <FiEyeOff /> : <FiEye />}
            </button>
          </div>

          {confirmPassword && (
            <p
              className={
                password === confirmPassword
                  ? "password-match"
                  : "password-mismatch"
              }
            >
              {password === confirmPassword
                ? "✓ Passwords match"
                : "✗ Passwords do not match"}
            </p>
          )}

          <button
            className="register-submit"
            type="submit"
            disabled={loading}
          >
            {loading ? t("loading") : t("auth.create_account")}
          </button>
        </form>

        {error && <p className="register-error">{error}</p>}
        {success && <p className="register-success">{success}</p>}

        <p className="register-footer">
          {t("auth.have_account")}{" "}
          <Link to="/login">{t("auth.login")}</Link>
        </p>
      </div>
    </div>
  );
}

export default RegisterPage;