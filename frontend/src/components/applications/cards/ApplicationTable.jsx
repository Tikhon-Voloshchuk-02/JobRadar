import StatusBadge from "../StatusBadge";
import "./ApplicationTable.css";

export default function ApplicationTable({
  applications,
  onSelect,
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
            <th></th>
          </tr>
        </thead>

        <tbody>
          {applications.map((app) => (
            <tr
              key={app.id}
              onClick={() => onSelect(app)}
            >
              <td>{app.company}</td>

              <td>{app.position}</td>

              <td>
                <StatusBadge status={app.status} />
              </td>

              <td>{app.appliedAt || "—"}</td>

              <td className="table-action-cell">
                ›
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}