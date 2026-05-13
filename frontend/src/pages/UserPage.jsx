import { useEffect, useState } from "react";
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
      .catch(() => setError("Could not load user profile"));
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

      toast.success("Profile updated");
    } catch (err) {
      console.error(err);
      toast.error("Could not update profile");
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
      toast.error("Could not connect Gmail");
    }
  }

  async function handleDisconnectGmail() {
    try {
      await disconnectGmail();

      const updatedUser = await getCurrentUser();
      setUser(updatedUser);

      toast.success("Gmail disconnected");
    } catch (err) {
      console.error(err);
      toast.error("Could not disconnect Gmail");
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
          ? "Auto-Update enabled"
          : "Auto-Update disabled"
      );
    } catch (err) {
      console.error(err);
      toast.error("Could not update Auto-Update setting");
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

      toast.success("Auto-Update enabled");
    } catch (err) {
      console.error(err);
      toast.error("Could not enable Auto-Update");
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
            <h1>Profile</h1>
            <p className="user-subtitle">Account overview and integrations</p>
          </div>
        </div>

        <div className="profile-grid">
          <div>
            <span>Name</span>

            {isEditingName ? (
              <div className="name-edit">
                <input
                  value={nameInput}
                  onChange={(e) => setNameInput(e.target.value)}
                  disabled={saving}
                />

                <button onClick={handleSaveName} disabled={saving || !nameInput.trim()}>
                  {saving ? "Saving..." : "Save"}
                </button>

                <button
                  className="secondary-button"
                  onClick={() => {
                    setNameInput(user.name || "");
                    setIsEditingName(false);
                  }}
                  disabled={saving}
                >
                  Cancel
                </button>
              </div>
            ) : (
              <div className="name-display">
                <strong>{user.name || "—"}</strong>

                <button
                  className="small-button"
                  onClick={() => setIsEditingName(true)}
                >
                  Edit
                </button>
              </div>
            )}
          </div>

          <div>
            <span>Email</span>
            <strong className="profile-value">{user.email}</strong>
          </div>

          <div>
            <span>Email verification</span>
            <strong>{user.emailVerified ? "Verified" : "Not verified"}</strong>
          </div>

          <div>
            <span>Gmail</span>
            <strong>{user.gmailConnected ? "Connected" : "Not connected"}</strong>
          </div>

          {user.gmailConnected && (
            <div>
              <span>Auto-Update</span>
              <strong>{user.autoUpdateEnabled ? "Enabled" : "Disabled"}</strong>
            </div>
          )}

          <div>
            <span>Created</span>
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
            Auto-Update automatically accepts high-confidence AI suggestions and updates
            application statuses. Use carefully.
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
                  ? "Updating..."
                  : user.autoUpdateEnabled
                    ? "Disable Auto-Update"
                    : "Enable Auto-Update"}
              </button>

              <button className="secondary-button" onClick={handleDisconnectGmail}>
                Disconnect Gmail
              </button>
            </>
          ) : (
            <button className="secondary-button" onClick={handleConnectGmail}>
              Connect Gmail
            </button>
          )}

          <button className="danger-button" onClick={handleLogout}>
            Logout
          </button>

          <button onClick={() => navigate("/dashboard")}>
            Back to Dashboard
          </button>
        </div>
      </div>

      {showAutoUpdateWarning && (
        <div className="modal-overlay">
          <div className="warning-modal">
            <h2>Enable Auto-Update?</h2>

            <p>
              Auto-Update can automatically accept high-confidence AI suggestions
              and change application statuses without manual confirmation.
            </p>

            <p>
              Incorrect email analysis may result in wrong status updates. Enable
              this feature only if you accept this risk.
            </p>

            <label className="checkbox-row">
              <input
                type="checkbox"
                checked={dontShowAgain}
                onChange={(e) => setDontShowAgain(e.target.checked)}
              />
              Don&apos;t show again
            </label>

            <div className="modal-actions">
              <button
                className="secondary-button"
                onClick={() => setShowAutoUpdateWarning(false)}
                disabled={updatingAutoUpdate}
              >
                Cancel
              </button>

              <button
                className="danger-button"
                onClick={confirmEnableAutoUpdate}
                disabled={updatingAutoUpdate}
              >
                {updatingAutoUpdate ? "Enabling..." : "Enable Anyway"}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}