import { render, screen } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { MemoryRouter } from 'react-router-dom';
import DashboardPage from '../pages/DashboardPage';
import { AuthProvider } from '../context/AuthContext';

vi.mock('../api/stockApi', () => ({
    fetchAllStocks: vi.fn(),
}));

vi.mock('../api/portfolioApi', () => ({
    fetchSummary: vi.fn(),
}));

import { fetchAllStocks } from '../api/stockApi';
import { fetchSummary } from '../api/portfolioApi';

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
        fetchSummary.mockResolvedValue({
            totalPortfolioValue: 100000,
            totalCostBasis: 0,
            totalMarketValue: 0,
            totalUnrealizedPnl: 0,
            cashBalance: 100000,
            dailyChange: 0,
            positionCount: 0,
        });
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

        // THYAO hem tabloda hem de Market Movers'ta görünebilir → getAllByText
        expect(await screen.findByText('Türk Hava Yolları')).toBeInTheDocument();
        expect(screen.getByText('Havacılık')).toBeInTheDocument();
        expect(screen.getAllByText('THYAO').length).toBeGreaterThan(0);
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