import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router-dom";
import toast from "react-hot-toast";
import "./UserPage.css";

import {
  getCurrentUser,
  updateCurrentUser,
  connectGmail,
  disconnectGmail,
  setGmailAutoUpdate
} from "../api/api";

export default function UserPage() {

  const { t } = useTranslation();
  const [user, setUser] = useState(null);
  const [error, setError] = useState("");

  const [isEditingName, setIsEditingName] = useState(false);
  const [nameInput, setNameInput] = useState("");
  const [saving, setSaving] = useState(false);

  const navigate = useNavigate();
  const [updatingAutoUpdate, setUpdatingAutoUpdate] = useState(false);
  const [showAutoUpdateWarning, setShowAutoUpdateWarning] = useState(false);

  const [dontShowAgain, setDontShowAgain] = useState(
    localStorage.getItem("hideAutoUpdateWarning") === "true"
  );

  useEffect(() => {
    getCurrentUser()
      .then((data) => {
        setUser(data);
        setNameInput(data.name || "");
      })
      .catch(() => setError(t("user_page.could_not_load_profile")));
  }, []);

  function handleLogout() {
    localStorage.removeItem("token");
    navigate("/login");
  }

  async function handleSaveName() {
    try {
      setSaving(true);

      const updatedUser = await updateCurrentUser({ name: nameInput });

      setUser(updatedUser);
      setIsEditingName(false);

      toast.success(t("user_page.profile_updated"));
    } catch (err) {
      console.error(err);
      toast.success(t("user_page.profile_updated"));
    } finally {
      setSaving(false);
    }
  }

  async function handleConnectGmail() {
    try {
      const data = await connectGmail();
      window.location.href = data.url;
    } catch (err) {
      console.error(err);
      toast.error(t("user_page.could_not_connect_gmail"));
    }
  }

  async function handleDisconnectGmail() {
    try {
      await disconnectGmail();

      const updatedUser = await getCurrentUser();
      setUser(updatedUser);

      toast.success(t("user_page.gmail_disconnected"));
    } catch (err) {
      console.error(err);
      toast.error(t("user_page.could_not_disconnect_gmail"));
    }
  }

  async function handleToggleAutoUpdate() {

    if (!user.autoUpdateEnabled && !dontShowAgain) {
      setShowAutoUpdateWarning(true);
      return;
    }

    try {
      setUpdatingAutoUpdate(true);

      const response = await setGmailAutoUpdate(!user.autoUpdateEnabled);

      setUser({
        ...user,
        autoUpdateEnabled: response.autoUpdateEnabled,
      });

      toast.success(
        response.autoUpdateEnabled
          ? t("user_page.auto_update_enabled")
          : t("user_page.auto_update_disabled")
      );
    } catch (err) {
      console.error(err);
      toast.error(t("user_page.could_not_update_auto_update"));
    } finally {
      setUpdatingAutoUpdate(false);
    }
  }

  async function confirmEnableAutoUpdate() {

    if (dontShowAgain) {
      localStorage.setItem("hideAutoUpdateWarning", "true");
    }

    setShowAutoUpdateWarning(false);

    try {
      setUpdatingAutoUpdate(true);

      const response = await setGmailAutoUpdate(true);

      setUser({
        ...user,
        autoUpdateEnabled: response.autoUpdateEnabled,
      });

      toast.success(t("user_page.auto_update_enabled"));
    } catch (err) {
      console.error(err);
      toast.error(t("user_page.could_not_enable_auto_update"));
    } finally {
      setUpdatingAutoUpdate(false);
    }
  }



  if (error) {
    return (
      <div className="user-page">
        <p className="user-error">{error}</p>
      </div>
    );
  }

  if (!user) {
    return (
      <div className="user-page">
        <p>Loading profile...</p>
      </div>
    );
  }



  return (
    <div className="user-page">
      <div className="user-card">
        <div className="user-header">
          <div className="avatar">
            {user.name
              ? user.name
                  .split(" ")
                  .map((n) => n[0])
                  .slice(0, 2)
                  .join("")
                  .toUpperCase()
              : "U"}
          </div>

          <div>
            <h1>{t("user_page.title")}</h1>
            <p className="user-subtitle">{t("user_page.subtitle")}</p>
          </div>
        </div>

        <div className="profile-grid">
          <div>
            <span>{t("user_page.name")}</span>

            {isEditingName ? (
              <div className="name-edit">
                <input
                  value={nameInput}
                  onChange={(e) => setNameInput(e.target.value)}
                  disabled={saving}
                />

                <button onClick={handleSaveName} disabled={saving || !nameInput.trim()}>
                  {saving ? t("user_page.saving") : t("user_page.save")}
                </button>

                <button
                  className="secondary-button"
                  onClick={() => {
                    setNameInput(user.name || "");
                    setIsEditingName(false);
                  }}
                  disabled={saving}
                >
                  {t("cancel")}
                </button>
              </div>
            ) : (
              <div className="name-display">
                <strong>{user.name || "—"}</strong>

                <button
                  className="small-button"
                  onClick={() => setIsEditingName(true)}
                >
                  {t("edit")}
                </button>
              </div>
            )}
          </div>

          <div>
            <span>{t("user_page.email")}</span>
            <strong className="profile-value">{user.email}</strong>
          </div>

          <div>
            <span>{t("user_page.email_verification")}</span>
            <strong>
              {user.emailVerified
                ? t("user_page.verified")
                : t("user_page.not_verified")}
            </strong>
          </div>

          <div>
            <span>{t("user_page.gmail")}</span>
            <strong>
              {user.gmailConnected
                ? t("user_page.connected")
                : t("user_page.not_connected")}
            </strong>
          </div>

          {user.gmailConnected && (
            <div>
              <span>{t("user_page.auto_update")}</span>
              <strong>
                {user.autoUpdateEnabled
                  ? t("user_page.enabled")
                  : t("user_page.disabled")}
              </strong>
            </div>
          )}

          <div>
            <span>{t("user_page.created")}</span>
            <strong>
              {user.createdAt
                ? new Date(user.createdAt).toLocaleDateString(undefined, {
                    year: "numeric",
                    month: "short",
                    day: "numeric",
                  })
                : "—"}
            </strong>
          </div>
        </div>

        {user.gmailConnected && (
          <div className="auto-update-warning">
            {t("user_page.auto_update_warning")}
          </div>
        )}

        <div className="user-actions">
          {user.gmailConnected ? (
            <>
              <button
                className={user.autoUpdateEnabled ? "danger-button" : "secondary-button"}
                onClick={handleToggleAutoUpdate}
                disabled={updatingAutoUpdate}
              >
                {updatingAutoUpdate
                  ? t("user_page.updating")
                  : user.autoUpdateEnabled
                    ? t("user_page.disable_auto_update")
                    : t("user_page.enable_auto_update")}
              </button>

              <button className="secondary-button" onClick={handleDisconnectGmail}>
                {t("user_page.disconnect_gmail")}
              </button>
            </>
          ) : (
            <button className="secondary-button" onClick={handleConnectGmail}>
              {t("user_page.connect_gmail")}
            </button>
          )}

          <button className="danger-button" onClick={handleLogout}>
            {t("logout")}
          </button>

          <button onClick={() => navigate("/dashboard")}>
            {t("user_page.back_to_dashboard")}
          </button>
        </div>
      </div>

      {showAutoUpdateWarning && (
        <div className="modal-overlay">
          <div className="warning-modal">
            <h2>{t("user_page.auto_update_modal_title")}</h2>

            <p>{t("user_page.auto_update_modal_text_1")}</p>

            <p>{t("user_page.auto_update_modal_text_2")}</p>

            <label className="checkbox-row">
              <input
                type="checkbox"
                checked={dontShowAgain}
                onChange={(e) => setDontShowAgain(e.target.checked)}
              />
              {t("user_page.dont_show_again")}
            </label>

            <div className="modal-actions">
              <button
                className="secondary-button"
                onClick={() => setShowAutoUpdateWarning(false)}
                disabled={updatingAutoUpdate}
              >
                {t("cancel")}
              </button>

              <button
                className="danger-button"
                onClick={confirmEnableAutoUpdate}
                disabled={updatingAutoUpdate}
              >
                {updatingAutoUpdate
                  ? t("user_page.enabling")
                  : t("user_page.enable_anyway")}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}