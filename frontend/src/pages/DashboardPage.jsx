import { useTranslation } from "react-i18next";
import { useState } from "react";

import { logout } from "../auth/auth";

import FilterBar from "../components/FilterBar";
import ApplicationCard from "../components/ApplicationCard";
import DashboardSummary from "../components/DashboardSummary";
import ApplicationForm from "../components/ApplicationForm";
import HistoryPanel from "../components/HistoryPanel";
import RecentActivity from "../components/RecentActivity";
import LanguageSwitcher from "../components/LanguageSwitcher";

import { useDashboard } from "../hooks/useDashboard";
import { useNavigate } from "react-router-dom";
import Sidebar from "../components/Sidebar";

import "./DashboardPage.css";


export default function DashboardPage() {
  const { t } = useTranslation();

  const [sidebarOpen, setSidebarOpen] = useState(true);
  const navigate = useNavigate();

  const {
    loading,
    error,

    summary,
    recentActivity,

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
    <div className={`app-layout ${sidebarOpen ? "sidebar-open" : "sidebar-closed"}`}>
      <Sidebar open={sidebarOpen} setOpen={setSidebarOpen} />

      <main className="dashboard-page">
        <header className="dashboard-header">
          <div>
            <h1>{t("dashboard.title")}</h1>
            <p>{t("dashboard.subtitle")}</p>
          </div>

          <div className="dashboard-header__actions">
            <LanguageSwitcher />

            <button onClick={() => navigate("/user")}>
              Profile
            </button>

            <button onClick={toggleForm}>
              {showForm ? t("cancel") : t("add_application")}
            </button>

            <button onClick={handleLogout}>{t("logout")}</button>
          </div>
        </header>

        <DashboardSummary summary={summary} />
        <RecentActivity activities={recentActivity} />

        {showForm && (
          <ApplicationForm
            formData={formData}
            onChange={handleFormChange}
            onSubmit={handleSubmitApplication}
            isEditing={!!editingApplicationId}
            applicationId={editingApplicationId}
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

        {loading && <p>{t("loading_applications")}</p>}
        {error && <p className="error-text">{error}</p>}

        {!loading && !error && filteredApplications.length === 0 && (
          <p>{t("no_applications_found")}</p>
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
      </main>
    </div>
  );
}