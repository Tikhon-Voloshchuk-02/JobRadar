export default function PipelineCard({ items }) {
  return (
    <div className="gh-card">
      <div className="gh-card-header">
        <span className="gh-card-title">Pipeline</span>
        <span className="gh-card-meta">conversion from applied</span>
      </div>
      <div className="pipeline">
        {items.map(({ label, count, pct, color }) => (
          <div key={label} className="pipeline-row">
            <div className="pipeline-row-header">
              <span className="pipeline-label">{label}</span>
              <span className="pipeline-count" style={{ color }}>{count}</span>
              <span className="pipeline-pct">{pct}%</span>
            </div>
            <div className="pipeline-track">
              <div className="pipeline-fill" style={{ width: `${pct}%`, background: color }} />
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
