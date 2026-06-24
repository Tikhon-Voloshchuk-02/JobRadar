import { STATUS_OPTIONS } from "../utils/statusOptions";
import { useTranslation } from "react-i18next";

export default function FilterBar({
  searchTerm,
  selectedStatus,
  onSearchChange,
  onStatusChange,
  onReset,
}) {
    const { t } = useTranslation();

  return (
      <div className="filter-bar">
        <input
          type="text"
          placeholder={t("search_placeholder")}
          value={searchTerm}
          onChange={(e) => onSearchChange(e.target.value)}
        />

        <select
          value={selectedStatus}
          onChange={(e) => onStatusChange(e.target.value)}
        >
          <option value="ALL">{t("status.all")}</option>

          {STATUS_OPTIONS.map((status) => (
            <option key={status} value={status}>
              {t(`status.${status.toLowerCase()}`)}
            </option>
          ))}
        </select>

        <button onClick={onReset}>{t("reset")}</button>
      </div>
    );
  }