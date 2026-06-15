import { useEffect, useRef } from "react";

import StatusBadge from "./StatusBadge";
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
  onStatusChange,
}) {
  const topScrollRef = useRef(null);
  const boardScrollRef = useRef(null);

  const groupedApplications = STATUS_COLUMNS.map((status) => ({
    status,
    items: applications.filter((app) => app.status === status),
  }));

  useEffect(() => {
    const topScroll = topScrollRef.current;
    const boardScroll = boardScrollRef.current;

    if (!topScroll || !boardScroll) return;

    const syncFromTop = () => {
      boardScroll.scrollLeft = topScroll.scrollLeft;
    };

    const syncFromBoard = () => {
      topScroll.scrollLeft = boardScroll.scrollLeft;
    };

    topScroll.addEventListener("scroll", syncFromTop);
    boardScroll.addEventListener("scroll", syncFromBoard);

    return () => {
      topScroll.removeEventListener("scroll", syncFromTop);
      boardScroll.removeEventListener("scroll", syncFromBoard);
    };
  }, []);

  return (
    <div className="kanban-scroll-shell">
      <div className="kanban-scroll-top" ref={topScrollRef}>
        <div className="kanban-scroll-top__inner" />
      </div>

      <div className="application-kanban" ref={boardScrollRef}>
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

                    <button
                      type="button"
                      onClick={(event) => {
                        event.stopPropagation();
                        onEdit(app);
                      }}
                    >
                      Edit
                    </button>
                  </div>
                </article>
              ))}
            </div>
          </section>
        ))}
      </div>
    </div>
  );
}