const API_BASE_URL = "http://localhost:8080";

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