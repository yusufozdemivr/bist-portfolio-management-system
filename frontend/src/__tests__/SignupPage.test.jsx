import { render, screen } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import { MemoryRouter } from 'react-router-dom';
import SignupPage from '../pages/SignupPage';
import { AuthProvider } from '../context/AuthContext';

function renderSignupPage() {
    return render(
        <MemoryRouter>
            <AuthProvider>
                <SignupPage />
            </AuthProvider>
        </MemoryRouter>
    );
}

describe('SignupPage', () => {
    it('renders signup form with all fields', () => {
        renderSignupPage();

        expect(screen.getByLabelText('Username')).toBeInTheDocument();
        expect(screen.getByLabelText('First Name')).toBeInTheDocument();
        expect(screen.getByLabelText('Last Name')).toBeInTheDocument();
        expect(screen.getByLabelText('Email')).toBeInTheDocument();
        expect(screen.getByLabelText('Password')).toBeInTheDocument();
        expect(screen.getByRole('button', { name: 'Create Account' })).toBeInTheDocument();
    });

    it('renders login navigation link', () => {
        renderSignupPage();

        expect(screen.getByText('Already have an account?')).toBeInTheDocument();
        expect(screen.getByRole('link', { name: 'Sign In' })).toHaveAttribute('href', '/login');
    });

    it('renders application title', () => {
        renderSignupPage();

        expect(screen.getByRole('heading', { name: 'Create Account' })).toBeInTheDocument();
    });
});