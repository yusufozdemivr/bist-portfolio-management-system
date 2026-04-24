import { render, screen } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import { MemoryRouter } from 'react-router-dom';
import LoginPage from '../pages/LoginPage';
import { AuthProvider } from '../context/AuthContext';

function renderLoginPage() {
    return render(
        <MemoryRouter>
            <AuthProvider>
                <LoginPage />
            </AuthProvider>
        </MemoryRouter>
    );
}

describe('LoginPage', () => {
    it('renders login form with all fields', () => {
        renderLoginPage();

        expect(screen.getByLabelText('Username')).toBeInTheDocument();
        expect(screen.getByLabelText('Password')).toBeInTheDocument();
        expect(screen.getByRole('button', { name: 'Sign In' })).toBeInTheDocument();
    });

    it('renders signup navigation link', () => {
        renderLoginPage();

        expect(screen.getByText("Don't have an account?")).toBeInTheDocument();
        expect(screen.getByRole('link', { name: 'Create Account' })).toHaveAttribute('href', '/signup');
    });

    it('renders application title', () => {
        renderLoginPage();

        expect(screen.getByText('BIST Portfolio')).toBeInTheDocument();
    });

    it('sign in button is disabled when fields are empty', () => {
        renderLoginPage();

        expect(screen.getByRole('button', { name: 'Sign In' })).toBeDisabled();
    });
});