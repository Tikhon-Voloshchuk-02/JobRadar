import { getToken } from "../auth/auth";

const API_URL = "/api";

function authHeaders() {
  return {
    Authorization: `Bearer ${getToken()}`,
  };
}

export async function getDocuments(applicationId) {
  const response = await fetch(`${API_URL}/applications/${applicationId}/documents`, {
    headers: authHeaders(),
  });

  if (!response.ok) {
    throw new Error("Failed to load documents");
  }

  return response.json();
}

export async function uploadDocument(applicationId, file, type) {
  const formData = new FormData();
  formData.append("file", file);
  formData.append("type", type);

  const response = await fetch(`${API_URL}/applications/${applicationId}/documents`, {
    method: "POST",
    headers: authHeaders(),
    body: formData,
  });

  if (!response.ok) {
    throw new Error("Failed to upload document");
  }
}

export async function deleteDocument(documentId) {
  const response = await fetch(`${API_URL}/documents/${documentId}`, {
    method: "DELETE",
    headers: authHeaders(),
  });

  if (!response.ok) {
    throw new Error("Failed to delete document");
  }
}

export async function downloadDocument(documentId, fileName = "document") {
  const response = await fetch(`${API_URL}/documents/${documentId}/download`, {
    method: "GET",
    headers: authHeaders(), // ← ВОТ ГЛАВНОЕ
  });

  if (!response.ok) {
    throw new Error("Failed to download document");
  }

  const blob = await response.blob();
  const url = window.URL.createObjectURL(blob);

  const a = document.createElement("a");
  a.href = url;
  a.download = fileName;
  document.body.appendChild(a);
  a.click();

  a.remove();
  window.URL.revokeObjectURL(url);
}