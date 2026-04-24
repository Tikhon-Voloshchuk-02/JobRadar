function formatTime(dateString) {
  const date = new Date(dateString);
  const diff = Math.floor((Date.now() - date.getTime()) / 1000);

  if (diff < 60) return "just now";
  if (diff < 3600) return `${Math.floor(diff / 60)} min ago`;
  if (diff < 86400) return `${Math.floor(diff / 3600)} h ago`;

  return date.toLocaleDateString();
}

function getIcon(type) {
  return type === "CREATED" ? "➕" : "🔄";
}

export default function RecentActivity({ activities }) {
  return (
    <div className="recent-activity">
      <h3>Recent activity</h3>

      {activities.length === 0 ? (
        <p>No recent activity yet.</p>
      ) : (
        <div className="timeline">
          {activities.map((activity, index) => (
            <div key={index} className="timeline-item">
              <div className="timeline-icon">
                {getIcon(activity.type)}
              </div>

              <div className="timeline-content">
                <div className="timeline-time">
                  {formatTime(activity.timestamp)}
                </div>

                <div className="timeline-title">
                  <strong>{activity.company}</strong> — {activity.position}
                </div>

                <div className="timeline-status">
                  {activity.type === "CREATED"
                    ? `Created → ${activity.newStatus}`
                    : `${activity.oldStatus} → ${activity.newStatus}`}
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}