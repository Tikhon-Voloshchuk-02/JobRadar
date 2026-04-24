import { useEffect, useMemo, useState } from "react";

import {
  getAllApplications,
  createApplication,
  updateApplication,
  deleteApplication,
  updateApplicationStatus,
  getApplicationHistory,
  getDashboardSummary,
  getRecentActivity
} from "../api/applications";

const EMPTY_FORM = {
  company: "",
  position: "",
  link: "",
  status: "SAVED",
  notes: "",
  appliedAt: "",
};

const EMPTY_SUMMARY = {
  totalApplications: 0,
  activeApplications: 0,
  interviews: 0,
  offers: 0,
  statusDistribution: {},
};

export function useDashboard() {
  const [applications, setApplications] = useState([]);
  const [summary, setSummary] = useState(EMPTY_SUMMARY);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const [searchTerm, setSearchTerm] = useState("");
  const [selectedStatus, setSelectedStatus] = useState("ALL");

  const [showForm, setShowForm] = useState(false);
  const [editingApplicationId, setEditingApplicationId] = useState(null);
  const [formData, setFormData] = useState(EMPTY_FORM);

  const [showHistory, setShowHistory] = useState(false);
  const [selectedApplication, setSelectedApplication] = useState(null);
  const [historyEntries, setHistoryEntries] = useState([]);

  const [recentActivity, setRecentActivity] = useState([]);

  useEffect(() => {
    loadApplications();
    loadSummary();
    loadRecentActivity();
  }, []);

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

  async function loadSummary() {
    try {
      const data = await getDashboardSummary();
      setSummary(data);
    } catch (err) {
      console.error("Failed to load dashboard summary", err);
    }
  }

  async function loadRecentActivity() {
    try {
      const data = await getRecentActivity();
      setRecentActivity(data);
    } catch (err) {
      console.error("Failed to load recent activity", err);
    }
  }

  function resetForm() {
    setFormData(EMPTY_FORM);
    setEditingApplicationId(null);
  }

  function toggleForm() {
    if (showForm) {
      setShowForm(false);
      resetForm();
      return;
    }

    resetForm();
    setShowForm(true);
  }

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
      await loadSummary();
      await loadRecentActivity();

      resetForm();
      setShowForm(false);
      setSelectedStatus("ALL");
      setSearchTerm("");
    } catch (err) {
      setError(err.message || "Failed to save application");
    }
  }

  async function handleDelete(id) {
    const confirmed = window.confirm("Delete this application?");
    if (!confirmed) return;

    try {
      setError("");

      await deleteApplication(id);
      await loadApplications();
      await loadSummary();
      await loadRecentActivity();
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

      await loadSummary();
      await loadRecentActivity();
    } catch (err) {
      setError(err.message || "Failed to update status");
    }
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

  return {
    loading,
    error,

    summary,

    searchTerm,
    selectedStatus,
    setSearchTerm,
    setSelectedStatus,

    showForm,
    formData,
    editingApplicationId,
    toggleForm,
    handleFormChange,
    handleSubmitApplication,

    filteredApplications,
    handleEdit,
    handleDelete,
    handleStatusChange,
    handleViewHistory,
    recentActivity,

    showHistory,
    selectedApplication,
    historyEntries,
    closeHistory: () => setShowHistory(false),
  };
}