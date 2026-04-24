import { logout } from "../auth/auth";

import FilterBar from "../components/FilterBar";
import ApplicationCard from "../components/ApplicationCard";
import DashboardSummary from "../components/DashboardSummary";
import ApplicationForm from "../components/ApplicationForm";
import HistoryPanel from "../components/HistoryPanel";

import { useDashboard } from "../hooks/useDashboard";

import "./DashboardPage.css";

export default function DashboardPage() {
  const {
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

    showHistory,
    selectedApplication,
    historyEntries,
    closeHistory,
  } = useDashboard();

  function handleLogout() {
    logout();
    window.location.href = "/login";
  }

  return (
    <div className="dashboard-page">
      <header className="dashboard-header">
        <div>
          <h1>JobRadar Dashboard</h1>
          <p>Track your applications in one place</p>
        </div>

        <div className="dashboard-header__actions">
          <button onClick={toggleForm}>
            {showForm ? "Cancel" : "Add application"}
          </button>

          <button onClick={handleLogout}>Logout</button>
        </div>
      </header>

      <DashboardSummary summary={summary} />

      {showForm && (
        <ApplicationForm
          formData={formData}
          onChange={handleFormChange}
          onSubmit={handleSubmitApplication}
          isEditing={!!editingApplicationId}
        />
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

      {showHistory && (
        <HistoryPanel
          selectedApplication={selectedApplication}
          historyEntries={historyEntries}
          onClose={closeHistory}
        />
      )}
    </div>
  );
}