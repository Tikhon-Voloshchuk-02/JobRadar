import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import {
  getDocuments,
  uploadDocument,
  deleteDocument,
  downloadDocument,
} from "../api/documents";

import "./DocumentSection.css";

function formatDocumentType(type, t) {
  if (type === "COVER_LETTER") return t("documents.types.cover_letter");
  if (type === "CV") return t("documents.types.cv");
  return t("documents.types.other");
}

function DocumentSection({ applicationId, editable = false }) {
  const { t } = useTranslation();

  const [documents, setDocuments] = useState([]);
  const [file, setFile] = useState(null);
  const [type, setType] = useState("CV");

  async function loadDocuments() {
    try {
      const data = await getDocuments(applicationId);
      setDocuments(data);
    } catch (e) {
      console.error(e);
    }
  }

  useEffect(() => {
    loadDocuments();
  }, [applicationId]);

  async function handleUpload() {
    if (!file) return;

    try {
      await uploadDocument(applicationId, file, type);
      setFile(null);
      await loadDocuments();
    } catch (e) {
      console.error(e);
    }
  }

  async function handleDelete(id) {
    try {
      setDocuments((prev) => prev.filter((doc) => doc.id !== id)); // оптимистично
      await deleteDocument(id);
      await loadDocuments();
    } catch (e) {
      console.error(e);
      await loadDocuments();
    }
  }

  return (
    <section className="document-section">
      <h4>{t("documents.title")}</h4>

      {editable && (
        <div className="document-upload">
          <input type="file" onChange={(e) => setFile(e.target.files[0])} />

          <select value={type} onChange={(e) => setType(e.target.value)}>
            <option value="CV">{t("documents.types.cv")}</option>
            <option value="COVER_LETTER">{t("documents.types.cover_letter")}</option>
            <option value="OTHER">{t("documents.types.other")}</option>
          </select>

          <button type="button" onClick={handleUpload}>
            {t("documents.upload")}
          </button>
        </div>
      )}

      {documents.length === 0 ? (
        <p className="document-empty">{t("documents.empty")}</p>
      ) : (
        <div className="document-list">
          {documents.map((doc) => (
            <div className="document-item" key={doc.id}>
              <div className="document-item__icon">📄</div>

              <div className="document-item__info">
                <span className="document-item__name">
                  {doc.originalFileName}
                </span>
                <span className="document-item__type">
                  {formatDocumentType(doc.type, t)}
                </span>
              </div>

              <div className="document-item__actions">
                <button
                  type="button"
                  onClick={() => downloadDocument(doc.id, doc.originalFileName)}
                >
                  {t("documents.download")}
                </button>

                {editable && (
                  <button
                    type="button"
                    className="document-item__delete"
                    onClick={() => handleDelete(doc.id)}
                  >
                    {t("documents.delete")}
                  </button>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </section>
  );
}

export default DocumentSection;