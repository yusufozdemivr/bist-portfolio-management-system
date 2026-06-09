const API_BASE_URL = 'http://localhost:8080/api/portfolio';

export async function fetchPortfolio(token) {
    const response = await fetch(API_BASE_URL, {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json',
        },
    });

    if (!response.ok) {
        const error = await response.json().catch(() => null);
        throw new Error(error?.message || 'Failed to fetch portfolio.');
    }

    return response.json();
}

export async function fetchSummary(token) {
    const response = await fetch(`${API_BASE_URL}/summary`, {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json',
        },
    });

    if (!response.ok) {
        const error = await response.json().catch(() => null);
        throw new Error(error?.message || 'Failed to fetch portfolio summary.');
    }

    return response.json();
}

export async function fetchTransactions(token, symbol) {
    const response = await fetch(`${API_BASE_URL}/${symbol}/transactions`, {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json',
        },
    });

    if (!response.ok) {
        const error = await response.json().catch(() => null);
        throw new Error(error?.message || `Failed to fetch transactions for ${symbol}.`);
    }

    return response.json();
}