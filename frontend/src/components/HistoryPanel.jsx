import { useTranslation } from "react-i18next";

export default function HistoryPanel({
  selectedApplication,
  historyEntries,
  onClose,
}) {
  const { t } = useTranslation();

  if (!selectedApplication) return null;

  return (
    <div className="history-panel">
      <h2>{t("history.title")}</h2>

      <p>
        <strong>{selectedApplication.company}</strong> —{" "}
        {selectedApplication.position}
      </p>

      {historyEntries.length === 0 ? (
        <p>{t("history.no_history")}</p>
      ) : (
        <ul>
          {historyEntries.map((entry) => (
            <li key={entry.id}>
              {t(`status.${entry.oldStatus.toLowerCase()}`)} →{" "}
              {t(`status.${entry.newStatus.toLowerCase()}`)} (
              {entry.changedAt ||
                entry.createdAt ||
                entry.changeDate ||
                t("history.no_date")}
              )
            </li>
          ))}
        </ul>
      )}

      <button onClick={onClose}>{t("close")}</button>
    </div>
  );
}