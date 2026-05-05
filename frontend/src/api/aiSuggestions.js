// src/api/aiSuggestions.js

const API_BASE = "/api/ai-suggestions";

function getAuthHeaders() {
  const token = localStorage.getItem("token");

  return {
    "Content-Type": "application/json",
    Authorization: `Bearer ${token}`,
  };
}

// GET /api/ai-suggestions/pending
export async function getPendingAiSuggestions() {
  const response = await fetch(`${API_BASE}/pending`, {
    method: "GET",
    headers: getAuthHeaders(),
  });

  if (!response.ok) {
    throw new Error("Failed to load AI suggestions");
  }

  return response.json();
}

// POST /api/ai-suggestions/{id}/accept
export async function acceptAiSuggestion(id) {
  const response = await fetch(`${API_BASE}/${id}/accept`, {
    method: "POST",
    headers: getAuthHeaders(),
  });

  if (!response.ok) {
    throw new Error("Failed to accept suggestion");
  }

  return response.json();
}

// POST /api/ai-suggestions/{id}/reject
export async function rejectAiSuggestion(id) {
  const response = await fetch(`${API_BASE}/${id}/reject`, {
    method: "POST",
    headers: getAuthHeaders(),
  });

  if (!response.ok) {
    throw new Error("Failed to reject suggestion");
  }

  return response.json();
}

// POST /api/ai-suggestions/fake-analyze
export async function analyzeFakeEmail(data) {
  const response = await fetch(`${API_BASE}/fake-analyze`, {
    method: "POST",
    headers: getAuthHeaders(),
    body: JSON.stringify(data),
  });

  if (!response.ok) {
    throw new Error("Failed to analyze fake email");
  }

  return response.json();
}