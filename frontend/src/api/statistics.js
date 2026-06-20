function getAuthHeaders() {
  const token = localStorage.getItem("token");
  return {
    "Content-Type": "application/json",
    Authorization: `Bearer ${token}`,
  };
}

// GET /api/statistics
export async function getStatistics() {
  const response = await fetch("/api/statistics", {
    method: "GET",
    headers: getAuthHeaders(),
  });

  if (!response.ok) {
    throw new Error("Failed to load statistics");
  }

  return response.json();
}
