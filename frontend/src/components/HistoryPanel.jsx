export default function HistoryPanel({
  selectedApplication,
  historyEntries,
  onClose,
}) {
  if (!selectedApplication) return null;

  return (
    <div className="history-panel">
      <h2>Status history</h2>

      <p>
        <strong>{selectedApplication.company}</strong> —{" "}
        {selectedApplication.position}
      </p>

      {historyEntries.length === 0 ? (
        <p>No history yet.</p>
      ) : (
        <ul>
          {historyEntries.map((entry) => (
            <li key={entry.id}>
              {entry.oldStatus} → {entry.newStatus} (
              {entry.changedAt || entry.createdAt || entry.changeDate || "No date"})
            </li>
          ))}
        </ul>
      )}

      <button onClick={onClose}>Close</button>
    </div>
  );
}