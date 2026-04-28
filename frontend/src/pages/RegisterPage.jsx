import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { useTranslation } from "react-i18next";

import { registerRequest } from "../api/api";

import "./RegisterPage.css";

function RegisterPage() {
  const navigate = useNavigate();
  const { t } = useTranslation();

  const [firstname, setFirstname] = useState("");
  const [lastname, setLastname] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e) {
    e.preventDefault();

    setError("");
    setSuccess("");
    setLoading(true);

    try {
      await registerRequest({
        firstname,
        lastname,
        email,
        password,
      });

      setSuccess(t("auth.check_email"));

      // clean form
      setFirstname("");
      setLastname("");
      setEmail("");
      setPassword("");

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

          <input
            type="password"
            placeholder={t("auth.password")}
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />

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