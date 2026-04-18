import { STATUS_OPTIONS } from "../utils/statusOptions";

export default function FilterBar({
  searchTerm,
  selectedStatus,
  onSearchChange,
  onStatusChange,
  onReset,
}) {
  return (
    <div className="filter-bar">
      <input
        type="text"
        placeholder="Search by company, position, notes..."
        value={searchTerm}
        onChange={(e) => onSearchChange(e.target.value)}
      />

      <select
        value={selectedStatus}
        onChange={(e) => onStatusChange(e.target.value)}
      >
        <option value="">All statuses</option>
        {STATUS_OPTIONS.map((status) => (
          <option key={status} value={status}>
            {status}
          </option>
        ))}
      </select>

      <button onClick={onReset}>Reset</button>
    </div>
  );
}