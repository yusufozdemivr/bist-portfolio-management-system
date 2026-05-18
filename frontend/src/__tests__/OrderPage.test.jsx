import { render, screen } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { MemoryRouter } from 'react-router-dom';
import OrderPage from '../pages/OrderPage';
import { AuthProvider } from '../context/AuthContext';

vi.mock('../api/orderApi', () => ({
    placeOrder: vi.fn(),
    fetchOrders: vi.fn(),
    cancelOrder: vi.fn(),
}));

vi.mock('../api/stockApi', () => ({
    fetchAllStocks: vi.fn(),
}));

import { fetchOrders } from '../api/orderApi';
import { fetchAllStocks } from '../api/stockApi';

function renderOrderPage() {
    return render(
        <MemoryRouter>
            <AuthProvider>
                <OrderPage />
            </AuthProvider>
        </MemoryRouter>
    );
}

describe('OrderPage', () => {
    beforeEach(() => {
        vi.clearAllMocks();
        fetchAllStocks.mockResolvedValue([]);
    });

    it('renders order form with all fields', () => {
        fetchOrders.mockReturnValue(new Promise(() => {}));
        renderOrderPage();

        expect(screen.getByText('Place Order')).toBeInTheDocument();
        expect(screen.getByPlaceholderText('e.g. THYAO')).toBeInTheDocument();
        expect(screen.getByText('Side')).toBeInTheDocument();
        expect(screen.getByText('Type')).toBeInTheDocument();
        expect(screen.getByPlaceholderText('0')).toBeInTheDocument();
    });

    it('renders order history table after loading', async () => {
        fetchOrders.mockResolvedValue([
            {
                id: '123e4567-e89b-12d3-a456-426614174000',
                symbol: 'THYAO',
                companyName: 'Türk Hava Yolları',
                side: 'BUY',
                orderType: 'MARKET',
                orderStatus: 'FILLED',
                requestedQty: 10,
                filledQty: 10,
                executionPrice: 300.00,
                commissionFee: 0.60,
                totalAmount: 3000.00,
                createdAt: '2025-05-01T10:30:00Z',
            },
        ]);

        renderOrderPage();

        expect(await screen.findByText('THYAO')).toBeInTheDocument();
        expect(screen.getByText('FILLED')).toBeInTheDocument();
        expect(screen.getAllByText('MARKET').length).toBeGreaterThanOrEqual(2);
        expect(screen.getAllByText('BUY').length).toBeGreaterThanOrEqual(2);
    });

    it('renders status filter dropdown', () => {
        fetchOrders.mockResolvedValue([]);
        renderOrderPage();

        expect(screen.getByLabelText('Filter by status')).toBeInTheDocument();
    });

    it('shows loading state initially', () => {
        fetchOrders.mockReturnValue(new Promise(() => {}));
        renderOrderPage();

        expect(screen.getByText('Loading orders...')).toBeInTheDocument();
    });

    it('shows empty state when no orders', async () => {
        fetchOrders.mockResolvedValue([]);
        renderOrderPage();

        expect(await screen.findByText('No orders found')).toBeInTheDocument();
    });

    it('shows error message on fetch failure', async () => {
        fetchOrders.mockRejectedValue(new Error('Network error'));
        renderOrderPage();

        expect(await screen.findByText('Network error')).toBeInTheDocument();
    });
});