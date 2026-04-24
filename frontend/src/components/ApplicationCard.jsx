import { STATUS_OPTIONS } from "../utils/statusOptions";
import { useTranslation } from "react-i18next";
import "./ApplicationCard.css";

function getStatusClass(status) {
  return `status-badge status-${status.toLowerCase()}`;
}

export default function ApplicationCard({
  application,
  onEdit,
  onDelete,
  onViewHistory,
  onStatusChange,
}) {
    const { t } = useTranslation();

  return (
      <div className="application-card">
        <div className="application-card__top">
          <div>
            <h3>{application.company}</h3>
            <p>{application.position}</p>
          </div>

          <div className="application-card__status-control">
            <span className={getStatusClass(application.status)}>
              {t(`status.${application.status.toLowerCase()}`)}
            </span>

            <select
              value={application.status}
              onChange={(e) =>
                onStatusChange?.(application.id, e.target.value)
              }
            >
              {STATUS_OPTIONS.filter((status) => status !== "ALL").map((status) => (
                <option key={status} value={status}>
                  {t(`status.${status.toLowerCase()}`)}
                </option>
              ))}
            </select>
          </div>
        </div>

        <div className="application-card__body">
          {application.link && (
            <p>
              <a href={application.link} target="_blank" rel="noreferrer">
                {t("open_vacancy")}
              </a>
            </p>
          )}

          <p><strong>{t("applied_at")}:</strong> {application.appliedAt || "—"}</p>
          <p><strong>{t("created_at")}:</strong> {application.createdAt || "—"}</p>
          <p><strong>{t("notes")}:</strong> {application.notes || t("no_notes")}</p>
        </div>

        <div className="application-card__actions">
          <button onClick={() => onEdit?.(application)}>{t("edit")}</button>
          <button onClick={() => onViewHistory?.(application)}>
            {t("history_button")}
          </button>
          <button onClick={() => onDelete?.(application.id)}>{t("delete")}</button>
        </div>
      </div>
    );
  }