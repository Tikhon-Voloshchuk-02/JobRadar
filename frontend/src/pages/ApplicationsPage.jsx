import { useState } from "react";
import { useTranslation } from "react-i18next";

import Sidebar from "../components/Sidebar";
import FilterBar from "../components/FilterBar";
import ApplicationTable from "../components/ApplicationTable";
import ApplicationKanban from "../components/ApplicationKanban";
import ApplicationViewSwitcher from "../components/ApplicationViewSwitcher";
import ApplicationDrawer from "../components/ApplicationDrawer";
import EditApplicationModal from "../components/EditApplicationModal";
import ApplicationForm from "../components/ApplicationForm";
import HistoryPanel from "../components/HistoryPanel";

import { useDashboard } from "../hooks/useDashboard";

import "./ApplicationsPage.css";

export default function ApplicationsPage() {
  const { t } = useTranslation();

  const [sidebarOpen, setSidebarOpen] = useState(true);
  const [drawerApplication, setDrawerApplication] = useState(null);
  const [viewMode, setViewMode] = useState("table");

  const {
    loading,
    error,

    searchTerm,
    selectedStatus,
    setSearchTerm,
    setSelectedStatus,

    showForm,
    showEditModal,
    formData,
    editingApplicationId,
    toggleForm,
    closeEditModal,
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
  } = useDashboard(t);

  return (
    <div className={`app-layout ${sidebarOpen ? "sidebar-open" : "sidebar-closed"}`}>
      <Sidebar open={sidebarOpen} setOpen={setSidebarOpen} />

      <main className="applications-page">
        <header className="applications-page__header">
          <div>
            <h1>Applications</h1>
            <p>Manage your job applications in table or kanban view.</p>
          </div>

          <button onClick={toggleForm}>
            {showForm ? t("cancel") : t("add_application")}
          </button>
        </header>

        {showForm && (
          <ApplicationForm
            formData={formData}
            onChange={handleFormChange}
            onSubmit={handleSubmitApplication}
            isEditing={false}
            applicationId={null}
          />
        )}

        <EditApplicationModal
          open={showEditModal}
          formData={formData}
          applicationId={editingApplicationId}
          onChange={handleFormChange}
          onSubmit={handleSubmitApplication}
          onClose={closeEditModal}
        />

        <section className="applications-workspace">
          <div className="applications-workspace__topbar">
            <ApplicationViewSwitcher
              viewMode={viewMode}
              onChange={setViewMode}
            />
          </div>

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

          {!loading && !error && filteredApplications.length > 0 && (
            <>
              {viewMode === "table" && (
                <ApplicationTable
                  applications={filteredApplications}
                  onSelect={setDrawerApplication}
                  onEdit={handleEdit}
                  onDelete={handleDelete}
                  onViewHistory={handleViewHistory}
                  onStatusChange={handleStatusChange}
                />
              )}

              {viewMode === "kanban" && (
                <ApplicationKanban
                  applications={filteredApplications}
                  onSelect={setDrawerApplication}
                  onEdit={handleEdit}
                  onStatusChange={handleStatusChange}
                />
              )}
            </>
          )}
        </section>

        {showHistory && (
          <HistoryPanel
            selectedApplication={selectedApplication}
            historyEntries={historyEntries}
            onClose={closeHistory}
          />
        )}

        <ApplicationDrawer
          application={drawerApplication}
          onClose={() => setDrawerApplication(null)}
          onEdit={handleEdit}
        />
      </main>
    </div>
  );
}