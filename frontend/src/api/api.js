const API_BASE_URL = "http://localhost:8080"

export async function loginRequest(email, password) {
    const response = await fetch(`${API_BASE_URL}/api/auth/login`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json", },
        body: JSON.stringify({ email, password }),
    });

    if(!response.ok){
        let errorMessage = "Login failed";
        try {
            const errorData = await response.json();
            errorMessage = errorData.error || errorData.message || errorMessage;
        } catch {
        // ignore json parse err
        }
        throw new Error(errorMessage);

    }
    return response.json();
}

export async function getApplications(token){
    const response = await fetch(`${API_BASE_URL}/api/applications`, {
        method: "GET",
        headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
            },
    });

    if (!response.ok) {
        let errorMessage = "Failed to load applications";

        try {
          const errorData = await response.json();
          errorMessage = errorData.error || errorData.message || errorMessage;
        } catch {
          // ignore json parse err
        }

        throw new Error(errorMessage);
      }

      return response.json();
}