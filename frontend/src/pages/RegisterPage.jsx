import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { FiEye, FiEyeOff } from "react-icons/fi";

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
        <h1>{t("auth.register")}</h1>

        <form className="register-form" onSubmit={handleSubmit}>
          <input
            type="text"
            placeholder={t("auth.first_name")}
            value={firstname}
            onChange={(e) => setFirstname(e.target.value)}
            required
          />

          <input
            type="text"
            placeholder={t("auth.last_name")}
            value={lastname}
            onChange={(e) => setLastname(e.target.value)}
            required
          />

          <input
            type="email"
            placeholder={t("auth.email")}
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
          />

          <div className="password-field">
            <input
              type={showPassword ? "text" : "password"}
              placeholder={t("auth.password")}
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />

            <button
              type="button"
              className="password-toggle"
              onClick={() => setShowPassword(!showPassword)}
            >
              {showPassword ? <FiEyeOff /> : <FiEye />}
            </button>
          </div>



          <div className="password-field">
            <input
              type={showConfirmPassword ? "text" : "password"}
              placeholder="Confirm password"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              required
            />

            <button
              type="button"
              className="password-toggle"
              onClick={() => setShowConfirmPassword(!showConfirmPassword)}
            >
              {showConfirmPassword ? <FiEyeOff /> : <FiEye />}
            </button>
          </div>



          <button type="submit" disabled={loading}>
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