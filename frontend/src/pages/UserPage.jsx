import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { getCurrentUser, updateCurrentUser  } from "../api/api";
import "./UserPage.css";

export default function UserPage() {
  const [user, setUser] = useState(null);
  const [error, setError] = useState("");

  const [isEditingName, setIsEditingName] = useState(false);
  const [nameInput, setNameInput] = useState("");
  const [saving, setSaving] = useState(false);

  const navigate = useNavigate();

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
    } catch (err) {
      console.error(err);
      setError("Could not update profile");
    } finally {
      setSaving(false);
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

                <button onClick={handleSaveName} disabled={saving}>
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
            <strong>
              {user.emailVerified ? "Verified" : "Not verified"}
            </strong>
          </div>

          <div>
            <span>Gmail</span>
            <strong>
              {user.gmailConnected ? "Connected" : "Not connected"}
            </strong>
          </div>

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

        <div className="user-actions">
          <button className="secondary-button" disabled>
            Connect Gmail
          </button>

          <button className="danger-button" onClick={handleLogout}>
            Logout
          </button>

          <button onClick={() => navigate("/dashboard")}>
            Back to Dashboard
          </button>
        </div>
      </div>
    </div>
  );
}