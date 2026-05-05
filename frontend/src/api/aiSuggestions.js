import { getToken } from "../auth/auth";

const API_BASE_URL = "/api/ai-suggestions";

function authHeaders() {
  return {
    Authorization: `Bearer ${getToken()}`,
    "Content-Type": "application/json",
  };
}

export async function getPendingAiSuggestions() {
  const response = await fetch(`${API_BASE_URL}/pending`, {
    method: "GET",
    headers: authHeaders(),
  });

  if (!response.ok) {
    throw new Error("Failed to load AI suggestions");
  }

  return response.json();
}

export async function acceptAiSuggestion(id) {
  const response = await fetch(`${API_BASE_URL}/${id}/accept`, {
    method: "POST",
    headers: authHeaders(),
  });

  if (!response.ok) {
    throw new Error("Failed to accept AI suggestion");
  }

  return response.json();
}

export async function rejectAiSuggestion(id) {
  const response = await fetch(`${API_BASE_URL}/${id}/reject`, {
    method: "POST",
    headers: authHeaders(),
  });

  if (!response.ok) {
    throw new Error("Failed to reject AI suggestion");
  }

  return response.json();
}