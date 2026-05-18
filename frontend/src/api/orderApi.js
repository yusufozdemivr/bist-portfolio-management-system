const API_BASE_URL = 'http://localhost:8080/api/orders';

export async function placeOrder(token, orderData) {
    const response = await fetch(API_BASE_URL, {
        method: 'POST',
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(orderData),
    });

    if (!response.ok) {
        const error = await response.json().catch(() => null);
        throw new Error(
            error?.message || 'Failed to place order.'
        );
    }

    return response.json();
}

export async function fetchOrders(token, status) {
    const url = status && status !== 'ALL'
        ? `${API_BASE_URL}?status=${status}`
        : API_BASE_URL;

    const response = await fetch(url, {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json',
        },
    });

    if (!response.ok) {
        const error = await response.json().catch(() => null);
        throw new Error(
            error?.message || 'Failed to fetch orders.'
        );
    }

    return response.json();
}

export async function cancelOrder(token, orderId) {
    const response = await fetch(`${API_BASE_URL}/${orderId}`, {
        method: 'DELETE',
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json',
        },
    });

    if (!response.ok) {
        const error = await response.json().catch(() => null);
        throw new Error(
            error?.message || 'Failed to cancel order.'
        );
    }

    return response.json();
}