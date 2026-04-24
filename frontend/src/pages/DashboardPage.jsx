import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function DashboardPage() {
    const { username, logout } = useAuth();
    const navigate = useNavigate();

    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    const stats = [
        { label: 'Portfolio Value', value: '₺0.00', sub: 'No positions yet' },
        { label: 'Account Balance', value: '₺100,000.00', sub: 'Available cash' },
        { label: 'Open Orders', value: '0', sub: 'No pending orders' },
        { label: 'Total P&L', value: '₺0.00', sub: '0.00%' },
    ];

    return (
        <div style={styles.container}>
            <nav style={styles.nav}>
                <div style={styles.navLeft}>
                    <div style={styles.navLogo}>
                        <svg width="22" height="22" viewBox="0 0 32 32" fill="none">
                            <path d="M4 24V8h6v6h4V8h6v16h-6v-6h-4v6H4z" fill="#fff" fillOpacity="0.9"/>
                            <path d="M22 8h6v16h-6V8z" fill="#4a9eff"/>
                        </svg>
                    </div>
                    <span style={styles.navTitle}>BIST Portfolio</span>
                </div>
                <div style={styles.navRight}>
                    <div style={styles.userChip}>
                        <div style={styles.avatar}>{username?.charAt(0).toUpperCase()}</div>
                        <span style={styles.navUsername}>{username}</span>
                    </div>
                    <div style={styles.navDivider}></div>
                    <button onClick={handleLogout} style={styles.logoutBtn}>Sign Out</button>
                </div>
            </nav>

            <main style={styles.main}>
                <section style={styles.hero}>
                    <h1 style={styles.heroTitle}>Welcome back, {username}</h1>
                    <p style={styles.heroSub}>Your portfolio overview at a glance</p>
                </section>

                <div style={styles.grid}>
                    {stats.map((s) => (
                        <div style={styles.card} key={s.label}>
                            <span style={styles.cardLabel}>{s.label}</span>
                            <span style={styles.cardValue}>{s.value}</span>
                            <span style={styles.cardSub}>{s.sub}</span>
                        </div>
                    ))}
                </div>

                <div style={styles.emptyState}>
                    <div style={styles.emptyIcon}>
                        <svg width="48" height="48" viewBox="0 0 48 48" fill="none">
                            <rect x="4" y="20" width="8" height="24" rx="2" fill="#e5e7eb"/>
                            <rect x="16" y="12" width="8" height="32" rx="2" fill="#d1d5db"/>
                            <rect x="28" y="16" width="8" height="28" rx="2" fill="#e5e7eb"/>
                            <rect x="40" y="8" width="4" height="36" rx="2" fill="#d1d5db"/>
                        </svg>
                    </div>
                    <h2 style={styles.emptyTitle}>Market data and trading coming soon</h2>
                    <p style={styles.emptyText}>
                        Live BIST100 prices, order management, and portfolio analytics
                        will appear here in the next update.
                    </p>
                </div>
            </main>
        </div>
    );
}

const styles = {
    container: {
        minHeight: '100vh',
        backgroundColor: '#f3f4f6',
    },
    nav: {
        height: '56px',
        background: 'linear-gradient(135deg, #0a1628, #122244)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        padding: '0 28px',
        boxShadow: '0 1px 3px rgba(0,0,0,0.2)',
    },
    navLeft: {
        display: 'flex',
        alignItems: 'center',
        gap: '10px',
    },
    navLogo: {
        display: 'flex',
        alignItems: 'center',
    },
    navTitle: {
        color: '#ffffff',
        fontSize: '16px',
        fontWeight: '700',
        letterSpacing: '-0.3px',
    },
    navRight: {
        display: 'flex',
        alignItems: 'center',
        gap: '14px',
    },
    userChip: {
        display: 'flex',
        alignItems: 'center',
        gap: '8px',
    },
    avatar: {
        width: '30px',
        height: '30px',
        borderRadius: '50%',
        backgroundColor: 'rgba(74,158,255,0.2)',
        color: '#4a9eff',
        fontSize: '13px',
        fontWeight: '600',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
    },
    navUsername: {
        color: 'rgba(255,255,255,0.75)',
        fontSize: '13px',
    },
    navDivider: {
        width: '1px',
        height: '20px',
        backgroundColor: 'rgba(255,255,255,0.15)',
    },
    logoutBtn: {
        padding: '6px 14px',
        backgroundColor: 'transparent',
        color: 'rgba(255,255,255,0.6)',
        border: '1px solid rgba(255,255,255,0.15)',
        borderRadius: '6px',
        cursor: 'pointer',
        fontSize: '12px',
        fontWeight: '500',
    },
    main: {
        maxWidth: '1060px',
        margin: '0 auto',
        padding: '32px 28px',
    },
    hero: {
        marginBottom: '28px',
    },
    heroTitle: {
        fontSize: '24px',
        fontWeight: '700',
        color: '#111827',
        marginBottom: '4px',
    },
    heroSub: {
        fontSize: '14px',
        color: '#6b7280',
    },
    grid: {
        display: 'grid',
        gridTemplateColumns: 'repeat(4, 1fr)',
        gap: '16px',
        marginBottom: '32px',
    },
    card: {
        backgroundColor: '#ffffff',
        borderRadius: '10px',
        padding: '22px',
        display: 'flex',
        flexDirection: 'column',
        gap: '4px',
        boxShadow: '0 1px 2px rgba(0,0,0,0.05)',
        border: '1px solid #f0f0f0',
    },
    cardLabel: {
        fontSize: '12px',
        fontWeight: '600',
        color: '#6b7280',
        textTransform: 'uppercase',
        letterSpacing: '0.5px',
    },
    cardValue: {
        fontSize: '22px',
        fontWeight: '700',
        color: '#111827',
        marginTop: '4px',
    },
    cardSub: {
        fontSize: '12px',
        color: '#9ca3af',
    },
    emptyState: {
        backgroundColor: '#ffffff',
        borderRadius: '10px',
        padding: '56px 32px',
        textAlign: 'center',
        border: '1px solid #f0f0f0',
    },
    emptyIcon: {
        marginBottom: '20px',
    },
    emptyTitle: {
        fontSize: '17px',
        fontWeight: '600',
        color: '#374151',
        marginBottom: '8px',
    },
    emptyText: {
        fontSize: '14px',
        color: '#9ca3af',
        maxWidth: '420px',
        margin: '0 auto',
        lineHeight: '1.6',
    },
};