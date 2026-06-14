import { createPortal } from "react-dom";

import StatusBadge from "./StatusBadge";
import "./ApplicationDrawer.css";

export default function ApplicationDrawer({
  application,
  onClose,
  onEdit,
}) {
  if (!application) return null;

  return createPortal(
    <div className="drawer-overlay" onClick={onClose}>
      <aside
        className="application-drawer"
        onClick={(event) => event.stopPropagation()}
      >
        <div className="drawer-header">
          <button className="drawer-close" onClick={onClose}>
            ×
          </button>

          <button
            className="drawer-edit-button"
            onClick={() => onEdit(application)}
          >
            Edit
          </button>
        </div>

        <div className="drawer-title">
          <h2>{application.company}</h2>
          <p>{application.position}</p>
        </div>

        <section className="drawer-section">
          <h3>Status</h3>
          <StatusBadge status={application.status} />
        </section>

        <section className="drawer-section">
          <h3>Applied</h3>
          <p>{application.appliedAt || "—"}</p>
        </section>

        <section className="drawer-section">
          <h3>Notes</h3>
          <p className="drawer-notes">
            {application.notes || "No notes available"}
          </p>
        </section>
      </aside>
    </div>,
    document.body
  );
}