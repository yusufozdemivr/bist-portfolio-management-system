import { render, screen } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { MemoryRouter } from 'react-router-dom';
import DashboardPage from '../pages/DashboardPage';
import { AuthProvider } from '../context/AuthContext';

vi.mock('../api/stockApi', () => ({
    fetchAllStocks: vi.fn(),
}));

import { fetchAllStocks } from '../api/stockApi';

function renderDashboard() {
    return render(
        <MemoryRouter>
            <AuthProvider>
                <DashboardPage />
            </AuthProvider>
        </MemoryRouter>
    );
}

describe('DashboardPage', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    it('shows loading state initially', () => {
        fetchAllStocks.mockReturnValue(new Promise(() => {}));
        renderDashboard();

        expect(screen.getByText('Loading stocks...')).toBeInTheDocument();
    });

    it('renders stock table after loading', async () => {
        fetchAllStocks.mockResolvedValue([
            {
                symbol: 'THYAO',
                companyName: 'Türk Hava Yolları',
                sector: 'Havacılık',
                lastPrice: 323.50,
                changePercentage: -1.07,
                dayHigh: 331.25,
                dayLow: 319.50,
                volume: 37829627,
            },
        ]);

        renderDashboard();

        expect(await screen.findByText('THYAO')).toBeInTheDocument();
        expect(screen.getByText('Türk Hava Yolları')).toBeInTheDocument();
        expect(screen.getByText('Havacılık')).toBeInTheDocument();
    });

    it('renders search input', () => {
        fetchAllStocks.mockResolvedValue([]);
        renderDashboard();

        expect(screen.getByLabelText('Search stocks')).toBeInTheDocument();
    });

    it('shows error message on fetch failure', async () => {
        fetchAllStocks.mockRejectedValue(new Error('Network error'));
        renderDashboard();

        expect(await screen.findByText('Network error')).toBeInTheDocument();
    });
});