export default function StageTimingsCard({ rows }) {
  return (
    <div className="gh-card">
      <div className="gh-card-header">
        <span className="gh-card-title">Avg. Time per Stage</span>
      </div>
      <div className="timings">
        {rows.length === 0 ? (
          <p style={{ color: "#8b949e", fontSize: 13 }}>No stage transitions yet</p>
        ) : (
          rows.map(({ label, days }) => (
            <div key={label} className="timing-row">
              <span className="timing-label">{label}</span>
              <span className="timing-days">{days > 0 ? `${days}d` : "—"}</span>
            </div>
          ))
        )}
      </div>
    </div>
  );
}
