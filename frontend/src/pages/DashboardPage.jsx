import { useEffect, useMemo, useState } from "react";

import {
  getAllApplications,
  createApplication,
  updateApplication,
  deleteApplication,
  updateApplicationStatus,
  getApplicationHistory,
   getDashboardSummary,
} from "../api/applications";

import { logout } from "../auth/auth";
import FilterBar from "../components/FilterBar";
import ApplicationCard from "../components/ApplicationCard";

import "./DashboardPage.css";

export default function DashboardPage() {
  const [applications, setApplications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [searchTerm, setSearchTerm] = useState("");
  const [selectedStatus, setSelectedStatus] = useState("ALL");

  const [showHistory, setShowHistory] = useState(false);
  const [selectedApplication, setSelectedApplication] = useState(null);
  const [historyEntries, setHistoryEntries] = useState([]);

  const [showForm, setShowForm] = useState(false);

  const [editingApplicationId, setEditingApplicationId] = useState(null);

  const [summary, setSummary] = useState({
    totalApplications: 0,
    activeApplications: 0,
    interviews: 0,
    offers: 0,
  });

    const [formData, setFormData] = useState({
      company: "",
      position: "",
      link: "",
      status: "SAVED",
      notes: "",
      appliedAt: "",
    });

  async function loadApplications() {
    try {
      setLoading(true);
      setError("");

      const data = await getAllApplications();
      setApplications(data);
    } catch (err) {
      setError(err.message || "Failed to load applications");
    } finally {
      setLoading(false);
    }
  }

//applications
  useEffect(() => {
    loadApplications();
  }, []);

  // summary loader
  async function loadSummary() {
    try {
      console.log("LOADING SUMMARY...");
      const data = await getDashboardSummary();
      console.log("SUMMARY DATA:", data);
      setSummary(data);
    } catch (error) {
      console.error("Failed to load dashboard summary", error);
    }
  }

  // summary
  useEffect(() => {
    console.log("SUMMARY EFFECT RUN");
    loadSummary();
  }, []);

  function handleFormChange(e) {
      const { name, value } = e.target;

      setFormData((prev) => ({
        ...prev,
        [name]: value,
      }));
  }

  function handleEdit(application) {
    setShowForm(true);
    setEditingApplicationId(application.id);

    setFormData({
      company: application.company || "",
      position: application.position || "",
      link: application.link || "",
      status: application.status || "SAVED",
      notes: application.notes || "",
      appliedAt: application.appliedAt || "",
    });
  }

  async function handleViewHistory(application) {
    try {
      setError("");
      const history = await getApplicationHistory(application.id);

      setSelectedApplication(application);
      setHistoryEntries(history);
      setShowHistory(true);
    } catch (err) {
      setError(err.message || "Failed to load status history");
    }
  }

  async function handleSubmitApplication(e) {
    e.preventDefault();

    try {
      setError("");

      if (editingApplicationId) {
        await updateApplication(editingApplicationId, formData);
      } else {
        await createApplication(formData);
      }

      await loadApplications();

      setFormData({
        company: "",
        position: "",
        link: "",
        status: "SAVED",
        notes: "",
        appliedAt: "",
      });

      setEditingApplicationId(null);
      setShowForm(false);
      setSelectedStatus("ALL");
      setSearchTerm("");
    } catch (err) {
      console.error(err);
      setError(err.message || "Failed to save application");
    }
  }

  async function handleDelete(id) {
    const confirmed = window.confirm("Delete this application?");
    if (!confirmed) return;

    try {
      setError("");
      await deleteApplication(id);
      setApplications((prev) => prev.filter((item) => item.id !== id));
    } catch (err) {
      setError(err.message || "Failed to delete application");
    }
  }

  async function handleStatusChange(id, newStatus) {
    try {
      setError("");
      const updated = await updateApplicationStatus(id, newStatus);

      setApplications((prev) =>
        prev.map((item) => (item.id === id ? updated : item))
      );
    } catch (err) {
      setError(err.message || "Failed to update status");
    }
  }

  function handleLogout() {
    logout();
    window.location.href = "/login";
  }

  const filteredApplications = useMemo(() => {
    return applications.filter((app) => {
      const matchesStatus =
        selectedStatus === "ALL" || app.status === selectedStatus;

      const term = searchTerm.trim().toLowerCase();
      const matchesSearch =
        !term ||
        app.company?.toLowerCase().includes(term) ||
        app.position?.toLowerCase().includes(term) ||
        app.notes?.toLowerCase().includes(term);

      return matchesStatus && matchesSearch;
    });
  }, [applications, searchTerm, selectedStatus]);

  return (
    <div className="dashboard-page">
      <header className="dashboard-header">
        <div>
          <h1>JobRadar Dashboard</h1>
          <p>Track your applications in one place</p>
        </div>

        <div className="dashboard-header__actions">
          <button
            onClick={() => {
              if (showForm) {
                setShowForm(false);
                setEditingApplicationId(null);
                setFormData({
                  company: "",
                  position: "",
                  link: "",
                  status: "SAVED",
                  notes: "",
                  appliedAt: "",
                });
              } else {
                setShowForm(true);
                setEditingApplicationId(null);
                setFormData({
                  company: "",
                  position: "",
                  link: "",
                  status: "SAVED",
                  notes: "",
                  appliedAt: "",
                });
              }
            }}
          >
            {showForm ? "Cancel" : "Add application"}
          </button>
          <button onClick={handleLogout}>Logout</button>
        </div>
      </header>

      <div className="dashboard-summary">
        <div className="summary-card">
          <h3>Total</h3>
          <p>{summary.totalApplications}</p>
        </div>

        <div className="summary-card">
          <h3>Active</h3>
          <p>{summary.activeApplications}</p>
        </div>

        <div className="summary-card">
          <h3>Interviews</h3>
          <p>{summary.interviews}</p>
        </div>

        <div className="summary-card">
          <h3>Offers</h3>
          <p>{summary.offers}</p>
        </div>
      </div>

      {showForm && (
        <form className="application-form" onSubmit={handleSubmitApplication}>
          <input
            type="text"
            name="company"
            placeholder="Company"
            value={formData.company}
            onChange={handleFormChange}
            required
          />

          <input
            type="text"
            name="position"
            placeholder="Position"
            value={formData.position}
            onChange={handleFormChange}
            required
          />

          <input
            type="url"
            name="link"
            placeholder="Vacancy link"
            value={formData.link}
            onChange={handleFormChange}
          />

          <select
            name="status"
            value={formData.status}
            onChange={handleFormChange}
          >
            <option value="SAVED">SAVED</option>
            <option value="APPLIED">APPLIED</option>
            <option value="WAITING">WAITING</option>
            <option value="INTERVIEW">INTERVIEW</option>
            <option value="REJECTED">REJECTED</option>
            <option value="OFFER">OFFER</option>
          </select>

          <textarea
            name="notes"
            placeholder="Notes"
            value={formData.notes}
            onChange={handleFormChange}
          />

          <input
            type="date"
            name="appliedAt"
            value={formData.appliedAt}
            onChange={handleFormChange}
          />

          <button type="submit">
            {editingApplicationId ? "Update application" : "Save application"}
          </button>
        </form>
      )}

      <FilterBar
        searchTerm={searchTerm}
        selectedStatus={selectedStatus}
        onSearchChange={setSearchTerm}
        onStatusChange={setSelectedStatus}
        onReset={() => {
          setSearchTerm("");
          setSelectedStatus("ALL");
        }}
      />

      {loading && <p>Loading applications...</p>}
      {error && <p className="error-text">{error}</p>}

      {!loading && !error && filteredApplications.length === 0 && (
        <p>No applications found.</p>
      )}

      <div className="applications-grid">
        {filteredApplications.map((application) => (
          <ApplicationCard
            key={application.id}
            application={application}
            onEdit={handleEdit}
            onViewHistory={handleViewHistory}
            onDelete={handleDelete}
            onStatusChange={handleStatusChange}
          />
        ))}
      </div>

      {showHistory && selectedApplication && (
        <div className="history-panel">
          <h2>Status history</h2>
          <p>
            <strong>{selectedApplication.company}</strong> —{" "}
            {selectedApplication.position}
          </p>

          {historyEntries.length === 0 ? (
            <p>No history yet.</p>
          ) : (
            <ul>
              {historyEntries.map((entry) => (
                <li key={entry.id}>
                  {entry.oldStatus} → {entry.newStatus} (
                  {entry.changedAt || entry.createdAt || entry.changeDate || "No date"})
                </li>
              ))}
            </ul>
          )}

          <button onClick={() => setShowHistory(false)}>Close</button>
        </div>
      )}
    </div>
  );
}