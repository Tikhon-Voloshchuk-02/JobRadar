import { FiSearch, FiFilter, FiPlus } from "react-icons/fi";
import "./ApplicationsToolbar.css";

export default function ApplicationsToolbar({
  search,
  status,
  selectedCompany,
  companies = [],
  sortMode,
  showFilters,
  onSearchChange,
  onStatusChange,
  onCompanyChange,
  onSortChange,
  onToggleFilters,
  onResetFilters,
  onAddApplication,
}) {
  const hasActiveFilters =
    search.trim().length > 0 ||
    status !== "ALL" ||
    selectedCompany !== "ALL" ||
    sortMode !== "NEWEST";

  return (
    <div className="applications-toolbar">
      <div className="toolbar-top">
        <div className="toolbar-search">
          <FiSearch />
          <input
            value={search}
            onChange={(e) => onSearchChange(e.target.value)}
            placeholder="Search applications..."
          />
        </div>

        <button className="toolbar-button" type="button" onClick={onToggleFilters}>
          <FiFilter />
          Filters
        </button>

        <select
          className="toolbar-select toolbar-sort"
          value={sortMode}
          onChange={(e) => onSortChange(e.target.value)}
        >
          <option value="NEWEST">Newest</option>
          <option value="OLDEST">Oldest</option>
          <option value="COMPANY_ASC">Company A-Z</option>
          <option value="COMPANY_DESC">Company Z-A</option>
        </select>

        <button className="toolbar-primary" type="button" onClick={onAddApplication}>
          <FiPlus />
          Add application
        </button>
      </div>

      {showFilters && (
        <div className="toolbar-filters">
          <select
            className="toolbar-select toolbar-status"
            value={status}
            onChange={(e) => onStatusChange(e.target.value)}
          >
            <option value="ALL">All statuses</option>
            <option value="SAVED">Saved</option>
            <option value="APPLIED">Applied</option>
            <option value="WAITING">Waiting</option>
            <option value="INTERVIEW">Interview</option>
            <option value="REJECTED">Rejected</option>
            <option value="OFFER">Offer</option>
          </select>

          <select
            className="toolbar-select toolbar-company"
            value={selectedCompany}
            onChange={(e) => onCompanyChange(e.target.value)}
          >
            <option value="ALL">All companies</option>
            {companies.map((company) => (
              <option key={company} value={company}>
                {company}
              </option>
            ))}
          </select>

          {hasActiveFilters && (
            <button
              className="toolbar-reset"
              type="button"
              onClick={onResetFilters}
            >
              Reset filters
            </button>
          )}
        </div>
      )}
    </div>
  );
}