import { useTranslation } from "react-i18next";
import DocumentSection from "./DocumentSection";

export default function ApplicationForm({
  formData,
  onChange,
  onSubmit,
  isEditing,
  applicationId,
}) {
  const { t } = useTranslation();

  return (
    <form className="application-form" onSubmit={onSubmit}>
      <input
        type="text"
        name="company"
        placeholder={t("company")}
        value={formData.company}
        onChange={onChange}
        required
      />

      <input
        type="text"
        name="position"
        placeholder={t("position")}
        value={formData.position}
        onChange={onChange}
        required
      />

      <input
        type="url"
        name="link"
        placeholder={t("vacancy_link")}
        value={formData.link}
        onChange={onChange}
      />

      <select
        name="status"
        value={formData.status}
        onChange={onChange}
      >
        <option value="SAVED">{t("status.saved")}</option>
        <option value="APPLIED">{t("status.applied")}</option>
        <option value="WAITING">{t("status.waiting")}</option>
        <option value="INTERVIEW">{t("status.interview")}</option>
        <option value="REJECTED">{t("status.rejected")}</option>
        <option value="OFFER">{t("status.offer")}</option>
      </select>

      <textarea
        name="notes"
        placeholder={t("notes")}
        value={formData.notes}
        onChange={onChange}
      />

      <input
        type="date"
        name="appliedAt"
        value={formData.appliedAt}
        onChange={onChange}
      />

      {/* Documents (only in edit mode) */}
      {isEditing && applicationId && (
        <DocumentSection applicationId={applicationId} editable={true} />
      )}

      <button type="submit">
        {isEditing
          ? t("update_application")
          : t("save_application")}
      </button>
    </form>
  );
}