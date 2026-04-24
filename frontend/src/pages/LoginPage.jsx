import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function LoginPage() {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [loading, setLoading] = useState(false);
    const { login, error, clearError } = useAuth();
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();

        if (!username.trim() || !password.trim()) {
            return;
        }

        setLoading(true);
        const success = await login(username.trim(), password);
        setLoading(false);

        if (success) {
            navigate('/dashboard');
        }
    };

    return (
        <div style={styles.container}>
            <div style={styles.leftPanel}>
                <div style={styles.brandContent}>
                    <div style={styles.logoMark}>
                        <svg width="32" height="32" viewBox="0 0 32 32" fill="none">
                            <path d="M4 24V8h6v6h4V8h6v16h-6v-6h-4v6H4z" fill="#fff" fillOpacity="0.9"/>
                            <path d="M22 8h6v16h-6V8z" fill="#4a9eff"/>
                        </svg>
                    </div>
                    <h1 style={styles.brandTitle}>BIST Portfolio</h1>
                    <p style={styles.brandSubtitle}>Portfolio Management System</p>
                    <div style={styles.brandDivider}></div>
                    <div style={styles.featureList}>
                        <div style={styles.featureItem}>
                            <div style={styles.featureDot}></div>
                            <span>Real-time BIST100 stock tracking</span>
                        </div>
                        <div style={styles.featureItem}>
                            <div style={styles.featureDot}></div>
                            <span>Simulated portfolio management</span>
                        </div>
                        <div style={styles.featureItem}>
                            <div style={styles.featureDot}></div>
                            <span>Order execution and P&L analysis</span>
                        </div>
                    </div>
                </div>
                <p style={styles.brandFooter}>Graduation Project — 2026</p>
            </div>

            <div style={styles.rightPanel}>
                <div style={styles.formContainer}>
                    <div style={styles.formHeader}>
                        <h2 style={styles.formTitle}>Welcome back</h2>
                        <p style={styles.formSubtitle}>Sign in to your account</p>
                    </div>

                    {error && (
                        <div style={styles.error}>
                            <svg width="16" height="16" viewBox="0 0 16 16" fill="none" style={{flexShrink: 0}}>
                                <circle cx="8" cy="8" r="8" fill="#dc2626"/>
                                <path d="M8 4v5M8 11v1" stroke="#fff" strokeWidth="1.5" strokeLinecap="round"/>
                            </svg>
                            <span>{error}</span>
                        </div>
                    )}

                    <form onSubmit={handleSubmit}>
                        <div style={styles.field}>
                            <label htmlFor="username" style={styles.label}>Username</label>
                            <input
                                id="username"
                                type="text"
                                value={username}
                                onChange={(e) => { setUsername(e.target.value); clearError(); }}
                                placeholder="Enter your username"
                                style={styles.input}
                                disabled={loading}
                            />
                        </div>

                        <div style={styles.field}>
                            <label htmlFor="password" style={styles.label}>Password</label>
                            <input
                                id="password"
                                type="password"
                                value={password}
                                onChange={(e) => { setPassword(e.target.value); clearError(); }}
                                placeholder="Enter your password"
                                style={styles.input}
                                disabled={loading}
                            />
                        </div>

                        <button
                            type="submit"
                            style={{
                                ...styles.button,
                                ...(loading || !username.trim() || !password.trim()
                                    ? styles.buttonDisabled : {}),
                            }}
                            disabled={loading || !username.trim() || !password.trim()}
                        >
                            {loading ? 'Signing in...' : 'Sign In'}
                        </button>
                    </form>

                    <p style={styles.linkText}>
                        Don't have an account?{' '}
                        <Link to="/signup" onClick={clearError} style={styles.link}>Create Account</Link>
                    </p>
                </div>
            </div>
        </div>
    );
}

const styles = {
    container: {
        display: 'flex',
        minHeight: '100vh',
    },
    leftPanel: {
        flex: '0 0 480px',
        background: 'linear-gradient(160deg, #0a1628 0%, #122244 50%, #0d1b36 100%)',
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'center',
        padding: '60px',
        position: 'relative',
    },
    brandContent: {},
    logoMark: {
        width: '52px',
        height: '52px',
        borderRadius: '14px',
        backgroundColor: 'rgba(74, 158, 255, 0.12)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        marginBottom: '28px',
    },
    brandTitle: {
        fontSize: '28px',
        fontWeight: '700',
        color: '#ffffff',
        marginBottom: '6px',
        letterSpacing: '-0.5px',
    },
    brandSubtitle: {
        fontSize: '15px',
        color: 'rgba(255,255,255,0.5)',
        fontWeight: '400',
    },
    brandDivider: {
        width: '40px',
        height: '2px',
        backgroundColor: '#4a9eff',
        margin: '32px 0',
        borderRadius: '1px',
    },
    featureList: {
        display: 'flex',
        flexDirection: 'column',
        gap: '16px',
    },
    featureItem: {
        display: 'flex',
        alignItems: 'center',
        gap: '12px',
        fontSize: '14px',
        color: 'rgba(255,255,255,0.65)',
    },
    featureDot: {
        width: '6px',
        height: '6px',
        borderRadius: '50%',
        backgroundColor: '#4a9eff',
        flexShrink: 0,
    },
    brandFooter: {
        position: 'absolute',
        bottom: '32px',
        left: '60px',
        fontSize: '12px',
        color: 'rgba(255,255,255,0.25)',
    },
    rightPanel: {
        flex: 1,
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        backgroundColor: '#f8f9fb',
        padding: '40px',
    },
    formContainer: {
        width: '100%',
        maxWidth: '380px',
    },
    formHeader: {
        marginBottom: '32px',
    },
    formTitle: {
        fontSize: '24px',
        fontWeight: '700',
        color: '#111827',
        marginBottom: '6px',
    },
    formSubtitle: {
        fontSize: '14px',
        color: '#6b7280',
    },
    error: {
        backgroundColor: '#fef2f2',
        color: '#b91c1c',
        padding: '12px 16px',
        borderRadius: '8px',
        marginBottom: '24px',
        fontSize: '13px',
        border: '1px solid #fecaca',
        display: 'flex',
        alignItems: 'center',
        gap: '10px',
        lineHeight: '1.4',
    },
    field: {
        marginBottom: '20px',
    },
    label: {
        display: 'block',
        fontSize: '13px',
        fontWeight: '600',
        color: '#374151',
        marginBottom: '6px',
    },
    input: {
        width: '100%',
        padding: '11px 14px',
        border: '1.5px solid #d1d5db',
        borderRadius: '8px',
        fontSize: '14px',
        color: '#111827',
        backgroundColor: '#ffffff',
        boxSizing: 'border-box',
        transition: 'border-color 0.15s ease',
    },
    button: {
        width: '100%',
        padding: '12px',
        backgroundColor: '#111827',
        color: '#ffffff',
        border: 'none',
        borderRadius: '8px',
        fontSize: '14px',
        fontWeight: '600',
        cursor: 'pointer',
        marginTop: '4px',
    },
    buttonDisabled: {
        opacity: 0.4,
        cursor: 'not-allowed',
    },
    linkText: {
        textAlign: 'center',
        marginTop: '24px',
        fontSize: '14px',
        color: '#6b7280',
    },
    link: {
        color: '#2563eb',
        textDecoration: 'none',
        fontWeight: '600',
    },
};