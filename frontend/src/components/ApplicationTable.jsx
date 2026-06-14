export default function ApplicationTable({
  applications,
  onEdit,
  onDelete,
  onViewHistory,
  onStatusChange,
}) {
  return (
    <div className="application-table-wrapper">
      <table className="application-table">
        <thead>
          <tr>
            <th>Company</th>
            <th>Position</th>
            <th>Status</th>
            <th>Applied</th>
            <th>Actions</th>
          </tr>
        </thead>

        <tbody>
          {applications.map((app) => (
            <tr key={app.id}>
              <td>{app.company}</td>
              <td>{app.position}</td>
              <td>{app.status}</td>
              <td>{app.appliedAt}</td>

              <td>
                <button onClick={() => onEdit(app)}>
                  Edit
                </button>

                <button onClick={() => onViewHistory(app.id)}>
                  History
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}