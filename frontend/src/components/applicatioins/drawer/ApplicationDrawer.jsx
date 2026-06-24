import { useEffect, useState } from "react";
import { createPortal } from "react-dom";

import StatusBadge from "./StatusBadge";
import { getDocuments, downloadDocument } from "../api/documents";
import "./ApplicationDrawer.css";

export default function ApplicationDrawer({
  application,
  onClose,
  onEdit,
  onDelete,
}) {
  const [documents, setDocuments] = useState([]);
  const [documentsLoading, setDocumentsLoading] = useState(false);
  const [documentsError, setDocumentsError] = useState("");

  useEffect(() => {
    if (!application?.id) return;

    setDocuments([]);
    setDocumentsError("");
    setDocumentsLoading(true);

    getDocuments(application.id)
      .then(setDocuments)
      .catch(() => setDocumentsError("Failed to load documents"))
      .finally(() => setDocumentsLoading(false));
  }, [application?.id]);

  if (!application) return null;

  return createPortal(
    <div className="drawer-overlay" onClick={onClose}>
      <aside
        className="application-drawer"
        onClick={(event) => event.stopPropagation()}
      >
        <div className="drawer-header">
          <button className="drawer-close" onClick={onClose}>
            ×
          </button>

          <button
            className="drawer-edit-button"
            onClick={() => onEdit(application)}
          >
            Edit
          </button>
        </div>

        <div className="drawer-title">
          <h2>
            {application.company}

            {documents.length > 0 && (
              <span className="drawer-doc-count">
                📎 {documents.length}
              </span>
            )}
          </h2>

          <p>{application.position}</p>
        </div>

        <section className="drawer-section">
          <h3>Status</h3>
          <StatusBadge status={application.status} />
        </section>

         <section className="drawer-section">
                  <h3>Vacancy</h3>

                  {application.link ? (
                    <a
                      className="drawer-vacancy-link"
                      href={application.link}
                      target="_blank"
                      rel="noopener noreferrer"
                    >
                      Open vacancy ↗
                    </a>
                  ) : (
                    <p>No vacancy link available</p>
                  )}
                </section>

        <section className="drawer-section">
          <h3>Applied</h3>
          <p>{application.appliedAt || "—"}</p>
        </section>

        <section className="drawer-section">
          <h3>Notes</h3>
          <p className="drawer-notes">
            {application.notes || "No notes available"}
          </p>
        </section>

        <section className="drawer-section">
          <h3>
            Documents
            {documents.length > 0 && ` (${documents.length})`}
          </h3>

          {documentsLoading && (
            <p className="drawer-empty-documents">
              Loading documents...
            </p>
          )}

          {documentsError && (
            <p className="drawer-documents-error">
              {documentsError}
            </p>
          )}

          {!documentsLoading && !documentsError && documents.length === 0 && (
            <p className="drawer-empty-documents">
              No documents attached
            </p>
          )}

          {!documentsLoading && !documentsError && documents.length > 0 && (
            <div className="drawer-documents">
              {documents.map((doc) => (
                <div key={doc.id} className="drawer-document-card">
                  <div className="drawer-document-icon">📄</div>

                  <div className="drawer-document-info">
                    <div className="drawer-document-name">
                      {doc.originalFileName || doc.name || "Document"}
                    </div>

                    <div className="drawer-document-type">
                      {doc.type || "OTHER"}
                    </div>
                  </div>

                  <button
                    type="button"
                    className="drawer-document-download"
                    onClick={() =>
                      downloadDocument(
                        doc.id,
                        doc.originalFileName || doc.name || "document"
                      )
                    }
                  >
                    Download
                  </button>
                </div>
              ))}
            </div>
          )}
        </section>

        <section className="drawer-danger-section">
          <button
            type="button"
            className="drawer-delete-button"
            onClick={() => onDelete(application.id)}
          >
            Delete application
          </button>
        </section>
      </aside>
    </div>,
    document.body
  );
}