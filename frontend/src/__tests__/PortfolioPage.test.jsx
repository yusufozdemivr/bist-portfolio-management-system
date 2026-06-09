import { render, screen } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { MemoryRouter } from 'react-router-dom';
import PortfolioPage from '../pages/PortfolioPage';
import { AuthProvider } from '../context/AuthContext';

vi.mock('../api/portfolioApi', () => ({
    fetchPortfolio: vi.fn(),
    fetchTransactions: vi.fn(),
}));

import { fetchPortfolio } from '../api/portfolioApi';

function renderPortfolio() {
    return render(
        <MemoryRouter>
            <AuthProvider>
                <PortfolioPage />
            </AuthProvider>
        </MemoryRouter>
    );
}

describe('PortfolioPage', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    it('shows loading state initially', () => {
        fetchPortfolio.mockReturnValue(new Promise(() => {}));
        renderPortfolio();

        expect(screen.getByText('Loading portfolio...')).toBeInTheDocument();
    });

    it('renders portfolio table after loading', async () => {
        fetchPortfolio.mockResolvedValue([
            {
                symbol: 'THYAO',
                companyName: 'Türk Hava Yolları',
                quantity: 10,
                averageCost: 100,
                currentPrice: 120,
                marketValue: 1200,
                unrealizedPnl: 200,
                pnlPercentage: 20,
            },
        ]);

        renderPortfolio();

        expect(await screen.findByText('THYAO')).toBeInTheDocument();
        expect(screen.getByText('Market Value')).toBeInTheDocument();
    });

    it('shows empty state when no positions', async () => {
        fetchPortfolio.mockResolvedValue([]);
        renderPortfolio();

        expect(await screen.findByText('No positions yet')).toBeInTheDocument();
    });

    it('shows error message on fetch failure', async () => {
        fetchPortfolio.mockRejectedValue(new Error('Network error'));
        renderPortfolio();

        expect(await screen.findByText('Network error')).toBeInTheDocument();
    });
});