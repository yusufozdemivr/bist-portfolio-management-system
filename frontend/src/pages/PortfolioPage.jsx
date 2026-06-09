import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { fetchPortfolio, fetchTransactions } from '../api/portfolioApi';

export default function PortfolioPage() {
    const { username, token, logout } = useAuth();
    const navigate = useNavigate();

    const [positions, setPositions] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const [expanded, setExpanded] = useState(null);
    const [txCache, setTxCache] = useState({});

    useEffect(() => {
        let cancelled = false;
        async function load() {
            try {
                setLoading(true);
                setError(null);
                const data = await fetchPortfolio(token);
                if (!cancelled) setPositions(data);
            } catch (err) {
                if (!cancelled) setError(err.message);
            } finally {
                if (!cancelled) setLoading(false);
            }
        }
        load();
        return () => { cancelled = true; };
    }, [token]);

    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    const toggleRow = useCallback(async (symbol) => {
        if (expanded === symbol) {
            setExpanded(null);
            return;
        }
        setExpanded(symbol);
        if (!txCache[symbol]) {
            setTxCache((prev) => ({
                ...prev,
                [symbol]: { loading: true, error: null, data: [] },
            }));
            try {
                const data = await fetchTransactions(token, symbol);
                setTxCache((prev) => ({
                    ...prev,
                    [symbol]: { loading: false, error: null, data },
                }));
            } catch (err) {
                setTxCache((prev) => ({
                    ...prev,
                    [symbol]: { loading: false, error: err.message, data: [] },
                }));
            }
        }
    }, [expanded, txCache, token]);

    const formatPrice = (v) => {
        if (v == null) return '—';
        return `₺${Number(v).toLocaleString('tr-TR', {
            minimumFractionDigits: 2, maximumFractionDigits: 2,
        })}`;
    };

    const formatQty = (v) => {
        if (v == null) return '—';
        return Number(v).toLocaleString('tr-TR', { maximumFractionDigits: 6 });
    };

    const formatPnl = (v) => {
        if (v == null) return '—';
        const n = Number(v);
        const sign = n > 0 ? '+' : n < 0 ? '-' : '';
        const abs = Math.abs(n).toLocaleString('tr-TR', {
            minimumFractionDigits: 2, maximumFractionDigits: 2,
        });
        return `${sign}₺${abs}`;
    };

    const formatPct = (v) => {
        if (v == null) return '—';
        const n = Number(v);
        const sign = n > 0 ? '+' : '';
        return `${sign}${n.toFixed(2)}%`;
    };

    const pnlColor = (v) => {
        if (v == null) return '#6b7280';
        const n = Number(v);
        return n > 0 ? '#16a34a' : n < 0 ? '#dc2626' : '#6b7280';
    };

    const formatDate = (iso) => {
        if (!iso) return '—';
        return new Date(iso).toLocaleString('tr-TR', {
            dateStyle: 'short', timeStyle: 'short',
        });
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
                    <div style={styles.navLinks}>
                        <button onClick={() => navigate('/dashboard')} style={styles.navLink}>
                            Dashboard
                        </button>
                        <button onClick={() => navigate('/orders')} style={styles.navLink}>
                            Orders
                        </button>
                        <button style={styles.navLinkActive}>Portfolio</button>
                    </div>
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
                    <h1 style={styles.heroTitle}>My Portfolio</h1>
                    <p style={styles.heroSub}>Your holdings with live profit &amp; loss</p>
                </section>

                {loading && (
                    <div style={styles.statusBox}>
                        <p style={styles.statusText}>Loading portfolio...</p>
                    </div>
                )}

                {error && (
                    <div style={styles.errorBox}>
                        <p style={styles.errorText}>{error}</p>
                    </div>
                )}

                {!loading && !error && positions.length === 0 && (
                    <div style={styles.emptyBox}>
                        <p style={styles.emptyTitle}>No positions yet</p>
                        <p style={styles.emptyText}>
                            You don't hold any stocks. Place an order to get started.
                        </p>
                        <button onClick={() => navigate('/orders')} style={styles.emptyBtn}>
                            Go to Orders
                        </button>
                    </div>
                )}

                {!loading && !error && positions.length > 0 && (
                    <div style={styles.tableWrapper}>
                        <table style={styles.table}>
                            <thead>
                                <tr>
                                    <th style={styles.th}></th>
                                    <th style={styles.th}>Symbol</th>
                                    <th style={{ ...styles.th, textAlign: 'right' }}>Quantity</th>
                                    <th style={{ ...styles.th, textAlign: 'right' }}>Avg Cost</th>
                                    <th style={{ ...styles.th, textAlign: 'right' }}>Current Price</th>
                                    <th style={{ ...styles.th, textAlign: 'right' }}>Market Value</th>
                                    <th style={{ ...styles.th, textAlign: 'right' }}>P&amp;L</th>
                                    <th style={{ ...styles.th, textAlign: 'right' }}>P&amp;L %</th>
                                </tr>
                            </thead>
                            <tbody>
                                {positions.map((pos) => {
                                    const isOpen = expanded === pos.symbol;
                                    const tx = txCache[pos.symbol];
                                    return (
                                        <FragmentRow
                                            key={pos.symbol}
                                            pos={pos}
                                            isOpen={isOpen}
                                            tx={tx}
                                            onToggle={() => toggleRow(pos.symbol)}
                                            formatPrice={formatPrice}
                                            formatQty={formatQty}
                                            formatPnl={formatPnl}
                                            formatPct={formatPct}
                                            pnlColor={pnlColor}
                                            formatDate={formatDate}
                                        />
                                    );
                                })}
                            </tbody>
                        </table>
                    </div>
                )}
            </main>
        </div>
    );
}

function FragmentRow({
    pos, isOpen, tx, onToggle,
    formatPrice, formatQty, formatPnl, formatPct, pnlColor, formatDate,
}) {
    return (
        <>
            <tr
                style={{ ...styles.tr, ...(isOpen ? styles.trActive : {}) }}
                onClick={onToggle}
            >
                <td style={styles.tdCaret}>{isOpen ? '▾' : '▸'}</td>
                <td style={styles.tdSymbol}>{pos.symbol}</td>
                <td style={{ ...styles.td, textAlign: 'right' }}>{formatQty(pos.quantity)}</td>
                <td style={{ ...styles.td, textAlign: 'right' }}>{formatPrice(pos.averageCost)}</td>
                <td style={{ ...styles.td, textAlign: 'right' }}>{formatPrice(pos.currentPrice)}</td>
                <td style={{ ...styles.td, textAlign: 'right', fontWeight: '600' }}>
                    {formatPrice(pos.marketValue)}
                </td>
                <td style={{ ...styles.td, textAlign: 'right', color: pnlColor(pos.unrealizedPnl), fontWeight: '600' }}>
                    {formatPnl(pos.unrealizedPnl)}
                </td>
                <td style={{ ...styles.td, textAlign: 'right', color: pnlColor(pos.pnlPercentage), fontWeight: '600' }}>
                    {formatPct(pos.pnlPercentage)}
                </td>
            </tr>

            {isOpen && (
                <tr>
                    <td colSpan="8" style={styles.expandCell}>
                        {tx?.loading && (
                            <p style={styles.txStatus}>Loading transactions...</p>
                        )}
                        {tx?.error && (
                            <p style={styles.txError}>{tx.error}</p>
                        )}
                        {tx && !tx.loading && !tx.error && tx.data.length === 0 && (
                            <p style={styles.txStatus}>No transactions for this stock.</p>
                        )}
                        {tx && !tx.loading && !tx.error && tx.data.length > 0 && (
                            <table style={styles.txTable}>
                                <thead>
                                    <tr>
                                        <th style={styles.txTh}>Date</th>
                                        <th style={styles.txTh}>Side</th>
                                        <th style={{ ...styles.txTh, textAlign: 'right' }}>Quantity</th>
                                        <th style={{ ...styles.txTh, textAlign: 'right' }}>Price</th>
                                        <th style={{ ...styles.txTh, textAlign: 'right' }}>Commission</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {tx.data.map((t, i) => (
                                        <tr key={i}>
                                            <td style={styles.txTd}>{formatDate(t.executedAt)}</td>
                                            <td style={styles.txTd}>
                                                <span style={{
                                                    ...styles.sideBadge,
                                                    ...(t.side === 'BUY' ? styles.sideBuy : styles.sideSell),
                                                }}>
                                                    {t.side}
                                                </span>
                                            </td>
                                            <td style={{ ...styles.txTd, textAlign: 'right' }}>{formatQty(t.quantity)}</td>
                                            <td style={{ ...styles.txTd, textAlign: 'right' }}>{formatPrice(t.price)}</td>
                                            <td style={{ ...styles.txTd, textAlign: 'right' }}>{formatPrice(t.commission)}</td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        )}
                    </td>
                </tr>
            )}
        </>
    );
}

const styles = {
    container: { minHeight: '100vh', backgroundColor: '#f3f4f6' },
    nav: {
        height: '56px',
        background: 'linear-gradient(135deg, #0a1628, #122244)',
        display: 'flex', alignItems: 'center', justifyContent: 'space-between',
        padding: '0 28px', boxShadow: '0 1px 3px rgba(0,0,0,0.2)',
    },
    navLeft: { display: 'flex', alignItems: 'center', gap: '10px' },
    navLogo: { display: 'flex', alignItems: 'center' },
    navTitle: {
        color: '#ffffff', fontSize: '16px', fontWeight: '700',
        letterSpacing: '-0.3px', marginRight: '20px',
    },
    navLinks: { display: 'flex', gap: '4px' },
    navLink: {
        padding: '6px 14px', backgroundColor: 'transparent',
        color: 'rgba(255,255,255,0.6)', border: 'none', borderRadius: '6px',
        cursor: 'pointer', fontSize: '13px', fontWeight: '500',
    },
    navLinkActive: {
        padding: '6px 14px', backgroundColor: 'rgba(74,158,255,0.15)',
        color: '#4a9eff', border: 'none', borderRadius: '6px',
        cursor: 'default', fontSize: '13px', fontWeight: '600',
    },
    navRight: { display: 'flex', alignItems: 'center', gap: '14px' },
    userChip: { display: 'flex', alignItems: 'center', gap: '8px' },
    avatar: {
        width: '30px', height: '30px', borderRadius: '50%',
        backgroundColor: 'rgba(74,158,255,0.2)', color: '#4a9eff',
        fontSize: '13px', fontWeight: '600',
        display: 'flex', alignItems: 'center', justifyContent: 'center',
    },
    navUsername: { color: 'rgba(255,255,255,0.75)', fontSize: '13px' },
    navDivider: { width: '1px', height: '20px', backgroundColor: 'rgba(255,255,255,0.15)' },
    logoutBtn: {
        padding: '6px 14px', backgroundColor: 'transparent',
        color: 'rgba(255,255,255,0.6)', border: '1px solid rgba(255,255,255,0.15)',
        borderRadius: '6px', cursor: 'pointer', fontSize: '12px', fontWeight: '500',
    },
    main: { maxWidth: '1200px', margin: '0 auto', padding: '32px 28px' },
    hero: { marginBottom: '24px' },
    heroTitle: { fontSize: '24px', fontWeight: '700', color: '#111827', marginBottom: '4px' },
    heroSub: { fontSize: '14px', color: '#6b7280' },
    statusBox: {
        backgroundColor: '#ffffff', borderRadius: '10px', padding: '48px 32px',
        textAlign: 'center', border: '1px solid #f0f0f0',
    },
    statusText: { fontSize: '15px', color: '#6b7280' },
    errorBox: {
        backgroundColor: '#fef2f2', borderRadius: '10px',
        padding: '16px 24px', border: '1px solid #fecaca',
    },
    errorText: { fontSize: '14px', color: '#dc2626' },
    emptyBox: {
        backgroundColor: '#ffffff', borderRadius: '10px', padding: '56px 32px',
        textAlign: 'center', border: '1px solid #f0f0f0',
    },
    emptyTitle: { fontSize: '18px', fontWeight: '700', color: '#111827', marginBottom: '6px' },
    emptyText: { fontSize: '14px', color: '#6b7280', marginBottom: '20px' },
    emptyBtn: {
        padding: '10px 20px', backgroundColor: '#4a9eff', color: '#fff',
        border: 'none', borderRadius: '8px', cursor: 'pointer',
        fontSize: '14px', fontWeight: '600',
    },
    tableWrapper: {
        backgroundColor: '#ffffff', borderRadius: '10px',
        border: '1px solid #f0f0f0', overflow: 'hidden', overflowX: 'auto',
    },
    table: { width: '100%', borderCollapse: 'collapse', fontSize: '13px' },
    th: {
        padding: '12px 16px', textAlign: 'left', fontWeight: '600', fontSize: '11px',
        color: '#6b7280', textTransform: 'uppercase', letterSpacing: '0.5px',
        borderBottom: '1px solid #f0f0f0', backgroundColor: '#fafafa', whiteSpace: 'nowrap',
    },
    tr: { borderBottom: '1px solid #f5f5f5', cursor: 'pointer' },
    trActive: { backgroundColor: '#f8fafc' },
    td: { padding: '10px 16px', color: '#374151', whiteSpace: 'nowrap' },
    tdCaret: { padding: '10px 16px', color: '#9ca3af', width: '24px' },
    tdSymbol: { padding: '10px 16px', color: '#111827', fontWeight: '700', whiteSpace: 'nowrap' },
    expandCell: { padding: '0 16px 16px 48px', backgroundColor: '#f8fafc' },
    txTable: { width: '100%', borderCollapse: 'collapse', fontSize: '12px', marginTop: '4px' },
    txTh: {
        padding: '8px 12px', textAlign: 'left', fontWeight: '600', fontSize: '10px',
        color: '#9ca3af', textTransform: 'uppercase', letterSpacing: '0.5px',
        borderBottom: '1px solid #eef0f2', whiteSpace: 'nowrap',
    },
    txTd: { padding: '8px 12px', color: '#374151', whiteSpace: 'nowrap' },
    txStatus: { fontSize: '13px', color: '#6b7280', padding: '12px 0' },
    txError: { fontSize: '13px', color: '#dc2626', padding: '12px 0' },
    sideBadge: {
        padding: '2px 8px', borderRadius: '4px', fontSize: '11px', fontWeight: '700',
    },
    sideBuy: { backgroundColor: 'rgba(22,163,74,0.12)', color: '#16a34a' },
    sideSell: { backgroundColor: 'rgba(220,38,38,0.12)', color: '#dc2626' },
};