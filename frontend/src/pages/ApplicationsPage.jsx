import { useState } from "react";
import { useTranslation } from "react-i18next";

import Sidebar from "../components/Sidebar";
import ApplicationToolbar from "../components/ApplicationsToolbar";
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

    selectedCompany,
    setSelectedCompany,
    sortMode,
    setSortMode,
    showFilters,
    setShowFilters,
    companies,
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
        </header>

        {showForm && (
          <div className="modal-overlay" onClick={toggleForm}>
            <div
              className="create-application-modal"
              onClick={(e) => e.stopPropagation()}
            >
              <div className="modal-header">
                <div>
                  <h2>Add application</h2>
                  <p>Create a new job application entry.</p>
                </div>

                <button type="button" className="modal-close" onClick={toggleForm}>
                  ×
                </button>
              </div>

              <ApplicationForm
                formData={formData}
                onChange={handleFormChange}
                onSubmit={handleSubmitApplication}
                isEditing={false}
                applicationId={null}
              />
            </div>
          </div>
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

          <ApplicationToolbar
            search={searchTerm}
            status={selectedStatus}
            selectedCompany={selectedCompany}
            companies={companies}
            sortMode={sortMode}
            showFilters={showFilters}
            onSearchChange={setSearchTerm}
            onStatusChange={setSelectedStatus}
            onCompanyChange={setSelectedCompany}
            onSortChange={setSortMode}
            onToggleFilters={() => setShowFilters((prev) => !prev)}
            onResetFilters={() => {
              setSearchTerm("");
              setSelectedStatus("ALL");
              setSelectedCompany("ALL");
              setSortMode("NEWEST");
            }}
            onAddApplication={toggleForm}
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
                  onDelete={handleDelete}
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
          onEdit={(application) => {
            setDrawerApplication(null);
            handleEdit(application);
          }}
          onDelete={async (id) => {
            await handleDelete(id);
            setDrawerApplication(null);
          }}
        />
      </main>
    </div>
  );
}