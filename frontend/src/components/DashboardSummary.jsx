import { useState } from "react";
import { useTranslation } from "react-i18next";

export default function DashboardSummary({ summary }) {
  const { t } = useTranslation();
  const [showDistribution, setShowDistribution] = useState(false);

  const statusDistribution = summary.statusDistribution || {};

  return (
    <>
      <div className="dashboard-summary">
        <div className="summary-card">
          <h3>{t("summary.total")}</h3>
          <p>{summary.totalApplications}</p>
        </div>

        <div className="summary-card">
          <h3>{t("summary.active")}</h3>
          <p>{summary.activeApplications}</p>
        </div>

        <div className="summary-card">
          <h3>{t("summary.interviews")}</h3>
          <p>{summary.interviews}</p>
        </div>

        <div className="summary-card">
          <h3>{t("summary.offers")}</h3>
          <p>{summary.offers}</p>
        </div>
      </div>

      <div className="summary-card status-distribution-card">
        <div className="recent-activity__header">
          <h3>{t("summary.status_distribution")}</h3>

          <button
            className="toggle-btn"
            onClick={() => setShowDistribution((prev) => !prev)}
          >
            {showDistribution ? "▲" : "▼"}
          </button>
        </div>

        {showDistribution && (
          <div className="status-distribution-grid">
            {Object.entries(statusDistribution).map(([status, count]) => (
              <div className="status-pill" key={status}>
                <span>{t(`status.${status.toLowerCase()}`)}</span>
                <strong>{count}</strong>
              </div>
            ))}
          </div>
        )}
      </div>
    </>
  );
}