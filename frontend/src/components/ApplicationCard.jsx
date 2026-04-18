import { STATUS_OPTIONS } from "../utils/statusOptions";

export default function ApplicationCard({
  application,
  onEdit,
  onDelete,
  onViewHistory,
  onStatusChange,
}) {
  return (
    <div className="application-card">
      <div className="application-card__top">
        <div>
          <h3>{application.company}</h3>
          <p>{application.position}</p>
        </div>

        <select
          value={application.status}
          onChange={(e) => onStatusChange(application.id, e.target.value)}
        >
          {STATUS_OPTIONS.map((status) => (
            <option key={status} value={status}>
              {status}
            </option>
          ))}
        </select>
      </div>

      <div className="application-card__body">
        {application.link && (
          <p>
            <a href={application.link} target="_blank" rel="noreferrer">
              Open vacancy
            </a>
          </p>
        )}

        <p><strong>Applied at:</strong> {application.appliedAt || "—"}</p>
        <p><strong>Created at:</strong> {application.createdAt || "—"}</p>
        <p><strong>Notes:</strong> {application.notes || "No notes"}</p>
      </div>

      <div className="application-card__actions">
        <button onClick={() => onEdit(application)}>Edit</button>
        <button onClick={() => onViewHistory(application)}>History</button>
        <button onClick={() => onDelete(application.id)}>Delete</button>
      </div>
    </div>
  );
}