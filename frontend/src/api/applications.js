import { getToken } from "../auth/auth";

const API_BASE = "http://localhost:8080/api/applications";

function getAuthHeaders() {
  const token = getToken();
  console.log("TOKEN:", token);

  return {
    "Content-Type": "application/json",
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
  };
}

export async function getAllApplications() {
  const response = await fetch(API_BASE, {
    method: "GET",
    headers: getAuthHeaders(),
  });

  if (!response.ok) {
    throw new Error("Failed to load applications");
  }

  return response.json();
}

export async function createApplication(applicationData) {
  const response = await fetch(API_BASE, {
    method: "POST",
    headers: getAuthHeaders(),
    body: JSON.stringify(applicationData),
  });

  if (!response.ok) {
    throw new Error("Failed to create application");
  }

  return await response.text();
}

export async function updateApplicationStatus(id, status) {
  const response = await fetch(`${API_BASE}/${id}/status`, {
    method: "PATCH",
    headers: getAuthHeaders(),
    body: JSON.stringify({ newStatus: status }),
  });

  if (!response.ok) {
    throw new Error("Failed to update status");
  }

  return response.json();
}

export async function updateApplication(id, applicationData) {
  const response = await fetch(`${API_BASE}/${id}`, {
    method: "PUT",
    headers: getAuthHeaders(),
    body: JSON.stringify(applicationData),
  });

  if (!response.ok) {
    throw new Error("Failed to update application");
  }

  return response.json();
}

export async function deleteApplication(id) {
  const response = await fetch(`${API_BASE}/${id}`, {
    method: "DELETE",
    headers: getAuthHeaders(),
  });

  if (!response.ok) {
    throw new Error("Failed to delete application");
  }
}

export async function getApplicationHistory(id) {
  const response = await fetch(`${API_BASE}/${id}/history`, {
    method: "GET",
    headers: getAuthHeaders(),
  });

  if (!response.ok) {
    throw new Error("Failed to load status history");
  }

  return response.json();
}