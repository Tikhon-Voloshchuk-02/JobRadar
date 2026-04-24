import { useState } from "react";
import { useTranslation } from "react-i18next";
import "./RecentActivity.css";

function formatTime(dateString, t) {
  const date = new Date(dateString);
  const diff = Math.floor((Date.now() - date.getTime()) / 1000);

  if (diff < 60) return t("time.just_now");
  if (diff < 3600) return `${Math.floor(diff / 60)} ${t("time.min_ago")}`;
  if (diff < 86400) return `${Math.floor(diff / 3600)} ${t("time.hour_ago")}`;

  return date.toLocaleDateString();
}

function getIcon(type) {
  return type === "CREATED" ? "➕" : "🔄";
}

export default function RecentActivity({ activities }) {
  const { t } = useTranslation();
  const [collapsed, setCollapsed] = useState(true); // 👈 свернуто по умолчанию

  return (
    <div className="recent-activity">

      {/* HEADER */}
      <div className="recent-activity__header">
        <h3>{t("activity.title")}</h3>

        <button
          className="toggle-btn"
          onClick={() => setCollapsed((prev) => !prev)}
        >
          {collapsed ? "▼" : "▲"}
        </button>
      </div>

      {/* CONTENT */}
      {!collapsed && (
        <>
          {activities.length === 0 ? (
            <p>{t("activity.no_activity")}</p>
          ) : (
            <div className="timeline">
              {activities.map((activity, index) => (
                <div key={index} className="timeline-item">
                  <div className="timeline-icon">
                    {getIcon(activity.type)}
                  </div>

                  <div className="timeline-content">
                    <div className="timeline-time">
                      {formatTime(activity.timestamp, t)}
                    </div>

                    <div className="timeline-title">
                      <strong>{activity.company}</strong> — {activity.position}
                    </div>

                    <div className="timeline-status">
                      {activity.type === "CREATED" || !activity.oldStatus
                        ? `${t("activity.created")} → ${t(`status.${activity.newStatus?.toLowerCase()}`)}`
                        : `${t(`status.${activity.oldStatus.toLowerCase()}`)} → ${t(`status.${activity.newStatus?.toLowerCase()}`)}`
                      }
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </>
      )}
    </div>
  );
}