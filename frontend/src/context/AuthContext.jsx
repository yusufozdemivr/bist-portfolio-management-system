import { createContext, useContext, useState, useCallback } from 'react';
import { loginApi, registerApi } from '../api/authApi';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
    const [token, setToken] = useState(null);
    const [username, setUsername] = useState(null);
    const [error, setError] = useState(null);

    const login = useCallback(async (usernameInput, password) => {
        try {
            setError(null);
            const data = await loginApi(usernameInput, password);
            setToken(data.token);
            setUsername(data.username);
            return true;
        } catch (err) {
            setError(err.message);
            return false;
        }
    }, []);

    const signup = useCallback(async (usernameInput, firstName, lastName, email, password) => {
        try {
            setError(null);
            const data = await registerApi(usernameInput, firstName, lastName, email, password);
            setToken(data.token);
            setUsername(data.username);
            return true;
        } catch (err) {
            setError(err.message);
            return false;
        }
    }, []);

    const logout = useCallback(() => {
        setToken(null);
        setUsername(null);
        setError(null);
    }, []);

    const clearError = useCallback(() => {
        setError(null);
    }, []);

    const value = { token, username, error, login, signup, logout, clearError };

    return (
        <AuthContext.Provider value={value}>
            {children}
        </AuthContext.Provider>
    );
}

export function useAuth() {
    const context = useContext(AuthContext);
    if (!context) {
        throw new Error('useAuth must be used within an AuthProvider');
    }
    return context;
}