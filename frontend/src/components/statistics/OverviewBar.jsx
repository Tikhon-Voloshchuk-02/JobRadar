export default function OverviewBar({ items, hoveredStatus, onHover }) {
  return (
    <div className="gh-card overview-bar">
      {items.map(({ value, label, color }) => (
        <div
          key={label}
          className={`overview-item${hoveredStatus === label ? " overview-item--active" : ""}`}
          onMouseEnter={() => onHover(label)}
          onMouseLeave={() => onHover(null)}
        >
          <span className="overview-value" style={{ color }}>{value}</span>
          <span className="overview-label">{label}</span>
        </div>
      ))}
    </div>
  );
}
