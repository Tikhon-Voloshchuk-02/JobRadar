import { useTranslation } from "react-i18next";
import ApplicationForm from "./ApplicationForm";
import "./EditApplicationModal.css";

export default function EditApplicationModal({
  open,
  formData,
  applicationId,
  onChange,
  onSubmit,
  onClose,
}) {
  const { t } = useTranslation();

  if (!open) return null;

  return (
    <div className="edit-modal-backdrop" onClick={onClose}>
      <div className="edit-modal" onClick={(e) => e.stopPropagation()}>
        <div className="edit-modal__header">
          <div>
            <h2>{t("edit_application")}</h2>
            <p>{t("edit_application_hint")}</p>
          </div>

          <button
            type="button"
            className="edit-modal__close"
            onClick={onClose}
            aria-label="Close modal"
          >
            ×
          </button>
        </div>

        <ApplicationForm
          formData={formData}
          onChange={onChange}
          onSubmit={onSubmit}
          isEditing={true}
          applicationId={applicationId}
        />
      </div>
    </div>
  );
}