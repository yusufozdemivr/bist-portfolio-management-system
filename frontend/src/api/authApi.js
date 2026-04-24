const API_BASE_URL = 'http://localhost:8080/api/auth';

export async function loginApi(username, password) {
    const response = await fetch(`${API_BASE_URL}/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password }),
    });

    if (!response.ok) {
        const error = await response.json().catch(() => null);
        throw new Error(
            error?.message || 'Login failed. Please try again.'
        );
    }

    return response.json();
}

export async function registerApi(username, firstName, lastName, email, password) {
    const response = await fetch(`${API_BASE_URL}/register`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, firstName, lastName, email, password }),
    });

    if (!response.ok) {
        const error = await response.json().catch(() => null);
        throw new Error(
            error?.message || 'Registration failed. Please try again.'
        );
    }

    return response.json();
}