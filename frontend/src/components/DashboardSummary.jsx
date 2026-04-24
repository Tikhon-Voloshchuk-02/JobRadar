export default function DashboardSummary({ summary }) {
  const statusDistribution = summary.statusDistribution || {};

  return (
    <>
      <div className="dashboard-summary">
        <div className="summary-card">
          <h3>Total</h3>
          <p>{summary.totalApplications}</p>
        </div>

        <div className="summary-card">
          <h3>Active</h3>
          <p>{summary.activeApplications}</p>
        </div>

        <div className="summary-card">
          <h3>Interviews</h3>
          <p>{summary.interviews}</p>
        </div>

        <div className="summary-card">
          <h3>Offers</h3>
          <p>{summary.offers}</p>
        </div>
      </div>

      <div className="summary-card status-distribution-card">
        <h3>Status distribution</h3>

        <div className="status-distribution-grid">
          {Object.entries(statusDistribution).map(([status, count]) => (
            <div className="status-pill" key={status}>
              <span>{status}</span>
              <strong>{count}</strong>
            </div>
          ))}
        </div>
      </div>
    </>
  );
}