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

  function handleSubmit(event) {
    event.preventDefault();
    onSubmit(event);
  }

  return (
    <div className="edit-modal-backdrop" onClick={onClose}>
      <div className="edit-modal" onClick={(event) => event.stopPropagation()}>
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

        <form className="edit-modal__content" onSubmit={handleSubmit}>
          <ApplicationForm
            formData={formData}
            onChange={onChange}
            onSubmit={onSubmit}
            isEditing={true}
            applicationId={applicationId}
            hideSubmitButton={true}
          />

          <div className="edit-modal__footer">
            <button
              type="button"
              className="edit-modal__button edit-modal__button--secondary"
              onClick={onClose}
            >
              Cancel
            </button>

            <button
              type="submit"
              className="edit-modal__button edit-modal__button--primary"
            >
              Save changes
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}