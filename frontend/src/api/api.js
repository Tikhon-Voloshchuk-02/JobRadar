const API_BASE_URL = "";

async function handleError(response, defaultMessage) {
  let errorMessage = defaultMessage;

  try {
    const errorData = await response.json();
    errorMessage = errorData.error || errorData.message || errorMessage;
  } catch {}

  throw new Error(errorMessage);
}

// <----- LOGIN --------->
export async function loginRequest(email, password) {
  const response = await fetch(`${API_BASE_URL}/api/auth/login`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({ email, password }),
  });

  if (!response.ok) {
    await handleError(response, "Login failed");
  }

  return response.json();
}

// <---- REG.USER ---->
export async function registerRequest(registerData) {
  const response = await fetch(`${API_BASE_URL}/api/auth/register`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(registerData),
  });

  if (!response.ok) {
    await handleError(response, "Registration failed");
  }

  return response.json();
}


// <------- GET APPLICATIONS ----->
export async function getApplications(token) {
  const response = await fetch(`${API_BASE_URL}/api/applications`, {
    method: "GET",
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });

  if (!response.ok) {
    await handleError(response, "Failed to load applications");
  }

  return response.json();
}

// <------- GET DASHBOARD-SUMMARY ----->
export async function getDashboardSummary() {
  const token = localStorage.getItem("token");

  const response = await fetch("/api/dashboard/summary", {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });

  if (!response.ok) {
    throw new Error("Failed to fetch dashboard summary");
  }

  return response.json();
}

// <--- RESEND VERIFICATION EMAIL --->
export async function resendVerificationEmail(email) {
  const response = await fetch(`${API_BASE_URL}/api/auth/resend-verification`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({ email }),
  });

  if (!response.ok) {
    await handleError(response, "Failed to resend verification email");
  }

  return response.json();
}

// <--- GET - USER-Page --->
export async function getCurrentUser() {
  const token = localStorage.getItem("token");

  const response = await fetch("/api/users/me", {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });

  if (!response.ok) {
    throw new Error("Failed to load user profile");
  }

  return response.json();
}

// <--- MODIFIED - USER-Page --->
export async function updateCurrentUser(data) {
  const token = localStorage.getItem("token");

  const response = await fetch("/api/users/me", {
    method: "PATCH",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify(data),
  });

  if (!response.ok) {
    throw new Error("Failed to update user profile");
  }

  return response.json();
}

// <---- CONNECTION GOOGLE-EMAIL ---->
export async function connectGmail() {
  const token = localStorage.getItem("token");

  const response = await fetch("/api/gmail/connect", {
    method: "GET",
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });

  if (!response.ok) {
    throw new Error("Could not start Gmail connection");
  }

  return response.json();
}

// <---- DISCONNECTION GOOGLE-EMAIL ---->
export async function disconnectGmail() {
  const token = localStorage.getItem("token");

  const response = await fetch("/api/gmail/disconnect", {
    method: "POST",
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });

  if (!response.ok) {
    throw new Error("Could not disconnect Gmail");
  }

  return response.json();
}

export async function setGmailAutoUpdate(enabled) {
  const token = localStorage.getItem("token");

  const response = await fetch(`${API_BASE_URL}/api/gmail/auto-update`, {
    method: "PATCH",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify({ enabled }),
  });

  if (!response.ok) {
    throw new Error("Could not update auto-update setting");
  }

  return response.json();
}

