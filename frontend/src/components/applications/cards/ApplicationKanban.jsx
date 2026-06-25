import StatusBadge from "../StatusBadge";
import "./ApplicationKanban.css";

const STATUS_COLUMNS = [
  "SAVED",
  "APPLIED",
  "WAITING",
  "INTERVIEW",
  "OFFER",
  "REJECTED",
];

export default function ApplicationKanban({
  applications,
  onSelect,
  onEdit,
  onDelete,
  onStatusChange,
}) {
  const groupedApplications = STATUS_COLUMNS.map((status) => ({
    status,
    items: applications.filter((app) => app.status === status),
  }));

  return (
    <div className="application-kanban">
      {groupedApplications.map((column) => (
        <section key={column.status} className="kanban-column">
          <div className="kanban-column__header">
            <StatusBadge status={column.status} />
            <span>{column.items.length}</span>
          </div>

          <div className="kanban-column__body">
            {column.items.length === 0 && (
              <p className="kanban-empty">No applications</p>
            )}

            {column.items.map((app) => (
              <article
                key={app.id}
                className="kanban-card"
                onClick={() => onSelect(app)}
              >
                <div>
                  <h3>{app.company}</h3>
                  <p>{app.position}</p>
                </div>

                <div className="kanban-card__meta">
                  <span>{app.appliedAt || "No date"}</span>

                  <div className="kanban-card__actions">
                    <button
                      type="button"
                      onClick={(event) => {
                        event.stopPropagation();
                        onEdit(app);
                      }}
                    >
                      Edit
                    </button>

                    <button
                      type="button"
                      className="delete-button"
                      onClick={(event) => {
                        event.stopPropagation();
                        onDelete(app.id);
                      }}
                    >
                      Delete
                    </button>
                  </div>
                </div>
              </article>
            ))}
          </div>
        </section>
      ))}
    </div>
  );
}