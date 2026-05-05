const API_BASE_URL = 'http://localhost:8080/api/stocks';

export async function fetchAllStocks(token) {
    const response = await fetch(API_BASE_URL, {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json',
        },
    });

    if (!response.ok) {
        const error = await response.json().catch(() => null);
        throw new Error(
            error?.message || 'Failed to fetch stocks.'
        );
    }

    return response.json();
}

export async function fetchStockBySymbol(token, symbol) {
    const response = await fetch(`${API_BASE_URL}/${symbol}`, {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json',
        },
    });

    if (!response.ok) {
        const error = await response.json().catch(() => null);
        throw new Error(
            error?.message || `Failed to fetch stock: ${symbol}`
        );
    }

    return response.json();
}