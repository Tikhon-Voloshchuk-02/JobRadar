import { useEffect, useMemo, useState } from "react";

import {
  getAllApplications,
  createApplication,
  updateApplication,
  deleteApplication,
  updateApplicationStatus,
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

  const [showForm, setShowForm] = useState(false);

  const [editingApplicationId, setEditingApplicationId] = useState(null);

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

  useEffect(() => {
    loadApplications();
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
            onViewHistory={(app) => console.log("History", app)}
            onDelete={handleDelete}
            onStatusChange={handleStatusChange}
          />
        ))}
      </div>
    </div>
  );
}