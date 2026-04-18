import { useEffect, useMemo, useState } from "react";
import {
  getAllApplications,
  deleteApplication,
  updateApplicationStatus,
} from "../api/applications";
import FilterBar from "../components/FilterBar";
import ApplicationCard from "../components/ApplicationCard";

import "./DashboardPage.css";

export default function DashboardPage() {
  const [applications, setApplications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [searchTerm, setSearchTerm] = useState("");
  const [selectedStatus, setSelectedStatus] = useState("");

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

  async function handleDelete(id) {
    const confirmed = window.confirm("Delete this application?");
    if (!confirmed) return;

    try {
      await deleteApplication(id);
      setApplications((prev) => prev.filter((item) => item.id !== id));
    } catch (err) {
      setError(err.message || "Failed to delete application");
    }
  }

  async function handleStatusChange(id, newStatus) {
    try {
      const updated = await updateApplicationStatus(id, newStatus);

      setApplications((prev) =>
        prev.map((item) => (item.id === id ? updated : item))
      );
    } catch (err) {
      setError(err.message || "Failed to update status");
    }
  }

  function handleLogout() {
    localStorage.removeItem("token");
    window.location.href = "/login";
  }

  const filteredApplications = useMemo(() => {
    return applications.filter((app) => {
      const matchesStatus =
        !selectedStatus || app.status === selectedStatus;

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
          <button>Add application</button>
          <button onClick={handleLogout}>Logout</button>
        </div>
      </header>

      <FilterBar
        searchTerm={searchTerm}
        selectedStatus={selectedStatus}
        onSearchChange={setSearchTerm}
        onStatusChange={setSelectedStatus}
        onReset={() => {
          setSearchTerm("");
          setSelectedStatus("");
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
            onEdit={(app) => console.log("Edit", app)}
            onViewHistory={(app) => console.log("History", app)}
            onDelete={handleDelete}
            onStatusChange={handleStatusChange}
          />
        ))}
      </div>
    </div>
  );
}