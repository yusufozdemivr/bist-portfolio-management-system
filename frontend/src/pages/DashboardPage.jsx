import { useState, useEffect, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { fetchAllStocks } from '../api/stockApi';

export default function DashboardPage() {
    const { username, token, logout } = useAuth();
    const navigate = useNavigate();

    const [stocks, setStocks] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [searchTerm, setSearchTerm] = useState('');

    useEffect(() => {
        let cancelled = false;

        async function loadStocks() {
            try {
                setLoading(true);
                setError(null);
                const data = await fetchAllStocks(token);
                if (!cancelled) {
                    setStocks(data);
                }
            } catch (err) {
                if (!cancelled) {
                    setError(err.message);
                }
            } finally {
                if (!cancelled) {
                    setLoading(false);
                }
            }
        }

        loadStocks();
        return () => { cancelled = true; };
    }, [token]);

    const filteredStocks = useMemo(() => {
        if (!searchTerm.trim()) return stocks;
        const term = searchTerm.toLowerCase();
        return stocks.filter(
            (s) =>
                s.symbol.toLowerCase().includes(term) ||
                s.companyName.toLowerCase().includes(term) ||
                (s.sector && s.sector.toLowerCase().includes(term))
        );
    }, [stocks, searchTerm]);

    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    const formatPrice = (price) => {
        if (price == null) return '—';
        return `₺${Number(price).toLocaleString('tr-TR', {
            minimumFractionDigits: 2,
            maximumFractionDigits: 2,
        })}`;
    };

    const formatVolume = (volume) => {
        if (volume == null) return '—';
        if (volume >= 1_000_000) {
            return `${(volume / 1_000_000).toFixed(1)}M`;
        }
        if (volume >= 1_000) {
            return `${(volume / 1_000).toFixed(1)}K`;
        }
        return volume.toLocaleString('tr-TR');
    };

    const formatChange = (change) => {
        if (change == null) return { text: '—', color: '#6b7280' };
        const num = Number(change);
        const sign = num > 0 ? '+' : '';
        const color = num > 0 ? '#16a34a' : num < 0 ? '#dc2626' : '#6b7280';
        return { text: `${sign}${num.toFixed(2)}%`, color };
    };

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
                    <p style={styles.heroSub}>BIST100 live stock prices</p>
                </section>

                <div style={styles.searchContainer}>
                    <input
                        type="text"
                        placeholder="Search by symbol, company or sector..."
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                        style={styles.searchInput}
                        aria-label="Search stocks"
                    />
                </div>

                {loading && (
                    <div style={styles.statusBox}>
                        <p style={styles.statusText}>Loading stocks...</p>
                    </div>
                )}

                {error && (
                    <div style={styles.errorBox}>
                        <p style={styles.errorText}>{error}</p>
                    </div>
                )}

                {!loading && !error && (
                    <div style={styles.tableWrapper}>
                        <table style={styles.table}>
                            <thead>
                                <tr>
                                    <th style={styles.th}>Symbol</th>
                                    <th style={styles.th}>Company</th>
                                    <th style={styles.th}>Sector</th>
                                    <th style={{ ...styles.th, textAlign: 'right' }}>Price</th>
                                    <th style={{ ...styles.th, textAlign: 'right' }}>Change</th>
                                    <th style={{ ...styles.th, textAlign: 'right' }}>High</th>
                                    <th style={{ ...styles.th, textAlign: 'right' }}>Low</th>
                                    <th style={{ ...styles.th, textAlign: 'right' }}>Volume</th>
                                </tr>
                            </thead>
                            <tbody>
                                {filteredStocks.map((stock) => {
                                    const change = formatChange(stock.changePercentage);
                                    return (
                                        <tr key={stock.symbol} style={styles.tr}>
                                            <td style={styles.tdSymbol}>{stock.symbol}</td>
                                            <td style={styles.td}>{stock.companyName}</td>
                                            <td style={styles.tdSector}>{stock.sector || '—'}</td>
                                            <td style={{ ...styles.td, textAlign: 'right', fontWeight: '600' }}>
                                                {formatPrice(stock.lastPrice)}
                                            </td>
                                            <td style={{
                                                ...styles.td,
                                                textAlign: 'right',
                                                color: change.color,
                                                fontWeight: '600',
                                            }}>
                                                {change.text}
                                            </td>
                                            <td style={{ ...styles.td, textAlign: 'right' }}>
                                                {formatPrice(stock.dayHigh)}
                                            </td>
                                            <td style={{ ...styles.td, textAlign: 'right' }}>
                                                {formatPrice(stock.dayLow)}
                                            </td>
                                            <td style={{ ...styles.td, textAlign: 'right' }}>
                                                {formatVolume(stock.volume)}
                                            </td>
                                        </tr>
                                    );
                                })}
                                {filteredStocks.length === 0 && (
                                    <tr>
                                        <td colSpan="8" style={styles.emptyRow}>
                                            No stocks found matching "{searchTerm}"
                                        </td>
                                    </tr>
                                )}
                            </tbody>
                        </table>
                    </div>
                )}
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
        maxWidth: '1200px',
        margin: '0 auto',
        padding: '32px 28px',
    },
    hero: {
        marginBottom: '24px',
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
    searchContainer: {
        marginBottom: '20px',
    },
    searchInput: {
        width: '100%',
        maxWidth: '400px',
        padding: '10px 16px',
        fontSize: '14px',
        border: '1px solid #d1d5db',
        borderRadius: '8px',
        outline: 'none',
        backgroundColor: '#ffffff',
        boxSizing: 'border-box',
    },
    statusBox: {
        backgroundColor: '#ffffff',
        borderRadius: '10px',
        padding: '48px 32px',
        textAlign: 'center',
        border: '1px solid #f0f0f0',
    },
    statusText: {
        fontSize: '15px',
        color: '#6b7280',
    },
    errorBox: {
        backgroundColor: '#fef2f2',
        borderRadius: '10px',
        padding: '16px 24px',
        border: '1px solid #fecaca',
    },
    errorText: {
        fontSize: '14px',
        color: '#dc2626',
    },
    tableWrapper: {
        backgroundColor: '#ffffff',
        borderRadius: '10px',
        border: '1px solid #f0f0f0',
        overflow: 'hidden',
        overflowX: 'auto',
    },
    table: {
        width: '100%',
        borderCollapse: 'collapse',
        fontSize: '13px',
    },
    th: {
        padding: '12px 16px',
        textAlign: 'left',
        fontWeight: '600',
        fontSize: '11px',
        color: '#6b7280',
        textTransform: 'uppercase',
        letterSpacing: '0.5px',
        borderBottom: '1px solid #f0f0f0',
        backgroundColor: '#fafafa',
        whiteSpace: 'nowrap',
    },
    tr: {
        borderBottom: '1px solid #f5f5f5',
    },
    td: {
        padding: '10px 16px',
        color: '#374151',
        whiteSpace: 'nowrap',
    },
    tdSymbol: {
        padding: '10px 16px',
        color: '#111827',
        fontWeight: '700',
        whiteSpace: 'nowrap',
    },
    tdSector: {
        padding: '10px 16px',
        color: '#9ca3af',
        fontSize: '12px',
        whiteSpace: 'nowrap',
    },
    emptyRow: {
        padding: '32px 16px',
        textAlign: 'center',
        color: '#9ca3af',
        fontSize: '14px',
    },
};