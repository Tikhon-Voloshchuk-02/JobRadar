import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router-dom";
import toast from "react-hot-toast";

import {
  FiUser,
  FiMail,
  FiShield,
  FiCalendar,
  FiZap,
  FiLogOut,
  FiArrowLeft,
  FiEdit2,
} from "react-icons/fi";

import Sidebar from "../components/applications/Sidebar";
import "./UserPage.css";

import {
  getCurrentUser,
  updateCurrentUser,
  connectGmail,
  disconnectGmail,
  setGmailAutoUpdate,
} from "../api/api";

export default function UserPage() {
  const { t } = useTranslation();
  const navigate = useNavigate();

  const [sidebarOpen, setSidebarOpen] = useState(true);
  const [user, setUser] = useState(null);
  const [error, setError] = useState("");

  const [isEditingName, setIsEditingName] = useState(false);
  const [nameInput, setNameInput] = useState("");
  const [saving, setSaving] = useState(false);

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
  }, [t]);

  function initials() {
    if (!user?.name) return "U";

    return user.name
      .split(" ")
      .map((part) => part[0])
      .slice(0, 2)
      .join("")
      .toUpperCase();
  }

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
      toast.error(t("user_page.could_not_update_profile"));
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
    if (!user.autoUpdateEnabled) {
      const hideWarning =
        localStorage.getItem("hideAutoUpdateWarning") === "true";

      if (!hideWarning) {
        setShowAutoUpdateWarning(true);
        return;
      }
    }

    try {
      setUpdatingAutoUpdate(true);

      const nextValue = !user.autoUpdateEnabled;
      const response = await setGmailAutoUpdate(nextValue);

      setUser({
        ...user,
        autoUpdateEnabled: response.autoUpdateEnabled,
      });

      toast.success(
        nextValue
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
      <div className={`app-layout ${sidebarOpen ? "sidebar-open" : "sidebar-closed"}`}>
        <Sidebar open={sidebarOpen} setOpen={setSidebarOpen} />
        <main className="user-page">
          <p className="user-error">{error}</p>
        </main>
      </div>
    );
  }

  if (!user) {
    return (
      <div className={`app-layout ${sidebarOpen ? "sidebar-open" : "sidebar-closed"}`}>
        <Sidebar open={sidebarOpen} setOpen={setSidebarOpen} />
        <main className="user-page">
          <p>Loading profile...</p>
        </main>
      </div>
    );
  }

  return (
    <div className={`app-layout ${sidebarOpen ? "sidebar-open" : "sidebar-closed"}`}>
      <Sidebar open={sidebarOpen} setOpen={setSidebarOpen} />

      <main className="user-page">
        <div className="user-shell">
          <header className="user-page-header">
            <div>
              <h1>{t("user_page.title")}</h1>
              <p>{t("user_page.subtitle")}</p>
            </div>

            <div className="avatar">{initials()}</div>
          </header>

          <section className="settings-section">
            <div className="section-header">
              <h2>Account</h2>
              <p>Manage your personal account information.</p>
            </div>

            <div className="settings-list">
              <div className="settings-row">
                <div className="settings-row-main">
                  <div className="settings-icon">
                    <FiUser />
                  </div>

                  <div>
                    <span>{t("user_page.name")}</span>

                    {isEditingName ? (
                      <div className="name-edit">
                        <input
                          value={nameInput}
                          onChange={(e) => setNameInput(e.target.value)}
                          disabled={saving}
                        />

                        <div className="inline-actions">
                          <button
                            onClick={handleSaveName}
                            disabled={saving || !nameInput.trim()}
                          >
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
                      </div>
                    ) : (
                      <strong>{user.name || "—"}</strong>
                    )}
                  </div>
                </div>

                {!isEditingName && (
                  <button
                    className="small-button"
                    onClick={() => setIsEditingName(true)}
                  >
                    <FiEdit2 />
                    {t("edit")}
                  </button>
                )}
              </div>

              <div className="settings-row">
                <div className="settings-row-main">
                  <div className="settings-icon">
                    <FiMail />
                  </div>

                  <div>
                    <span>{t("user_page.email")}</span>
                    <strong>{user.email}</strong>
                  </div>
                </div>
              </div>

              <div className="settings-row">
                <div className="settings-row-main">
                  <div className="settings-icon success">
                    <FiShield />
                  </div>

                  <div>
                    <span>{t("user_page.email_verification")}</span>
                    <span className={user.emailVerified ? "status-chip success" : "status-chip warning"}>
                      {user.emailVerified
                        ? t("user_page.verified")
                        : t("user_page.not_verified")}
                    </span>
                  </div>
                </div>
              </div>

              <div className="settings-row">
                <div className="settings-row-main">
                  <div className="settings-icon">
                    <FiCalendar />
                  </div>

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
              </div>
            </div>
          </section>

          <section className="settings-section">
            <div className="section-header">
              <h2>Integrations</h2>
              <p>Connect and manage external services.</p>
            </div>

            <div className="settings-list">
              <div className="settings-row">
                <div className="settings-row-main">
                  <div className="settings-icon gmail-icon">M</div>

                  <div>
                    <strong>{t("user_page.gmail")}</strong>
                    <p className="row-description">
                      Receive and analyze emails from Gmail.
                    </p>
                  </div>
                </div>

                <div className="settings-row-actions">
                  <span className={user.gmailConnected ? "status-chip success" : "status-chip muted"}>
                    {user.gmailConnected
                      ? t("user_page.connected")
                      : t("user_page.not_connected")}
                  </span>

                  {user.gmailConnected ? (
                    <button className="danger-outline-button" onClick={handleDisconnectGmail}>
                      {t("user_page.disconnect_gmail")}
                    </button>
                  ) : (
                    <button className="secondary-button" onClick={handleConnectGmail}>
                      {t("user_page.connect_gmail")}
                    </button>
                  )}
                </div>
              </div>

              {user.gmailConnected && (
                <div className="settings-row">
                  <div className="settings-row-main">
                    <div className="settings-icon warning">
                      <FiZap />
                    </div>

                    <div>
                      <strong>{t("user_page.auto_update")}</strong>
                      <p className="row-description">
                        Automatically accepts high-confidence AI suggestions and updates application statuses.
                      </p>
                    </div>
                  </div>

                  <div className="settings-row-actions">
                    <span className={user.autoUpdateEnabled ? "status-chip success" : "status-chip warning"}>
                      {user.autoUpdateEnabled
                        ? t("user_page.enabled")
                        : t("user_page.disabled")}
                    </span>

                    <button
                      className={user.autoUpdateEnabled ? "danger-outline-button" : "secondary-button"}
                      onClick={handleToggleAutoUpdate}
                      disabled={updatingAutoUpdate}
                    >
                      {updatingAutoUpdate
                        ? t("user_page.updating")
                        : user.autoUpdateEnabled
                          ? t("user_page.disable_auto_update")
                          : t("user_page.enable_auto_update")}
                    </button>
                  </div>
                </div>
              )}

              {user.gmailConnected && (
                <div className="settings-warning">
                  {t("user_page.auto_update_warning")}
                </div>
              )}
            </div>
          </section>

          <section className="settings-section danger-zone">
            <div className="section-header">
              <h2>Session</h2>
              <p>Manage your current session.</p>
            </div>

            <div className="user-actions">
              <button onClick={() => navigate("/dashboard")}>
                <FiArrowLeft />
                {t("user_page.back_to_dashboard")}
              </button>

              <button className="danger-button" onClick={handleLogout}>
                <FiLogOut />
                {t("logout")}
              </button>
            </div>
          </section>
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
      </main>
    </div>
  );
}