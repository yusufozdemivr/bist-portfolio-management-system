import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { fetchAllStocks } from '../api/stockApi';
import { placeOrder, fetchOrders, cancelOrder } from '../api/orderApi';

export default function OrderPage() {
    const { username, token, logout } = useAuth();
    const navigate = useNavigate();

    // Form state
    const [symbol, setSymbol] = useState('');
    const [side, setSide] = useState('BUY');
    const [orderType, setOrderType] = useState('MARKET');
    const [quantity, setQuantity] = useState('');
    const [limitPrice, setLimitPrice] = useState('');
    const [stocks, setStocks] = useState([]);
    const [formError, setFormError] = useState(null);
    const [formSuccess, setFormSuccess] = useState(null);
    const [submitting, setSubmitting] = useState(false);

    // Order history state
    const [orders, setOrders] = useState([]);
    const [statusFilter, setStatusFilter] = useState('ALL');
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const loadOrders = useCallback(async () => {
        try {
            setLoading(true);
            setError(null);
            const data = await fetchOrders(token, statusFilter);
            setOrders(data);
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    }, [token, statusFilter]);

    useEffect(() => {
        loadOrders();
    }, [loadOrders]);

    useEffect(() => {
        async function loadStocks() {
            try {
                const data = await fetchAllStocks(token);
                setStocks(data);
            } catch (err) {
                // Stok listesi yüklenemezse form yine de çalışır
            }
        }
        loadStocks();
    }, [token]);

    const handleSubmit = async () => {
        setFormError(null);
        setFormSuccess(null);

        if (!symbol.trim()) {
            setFormError('Symbol is required');
            return;
        }
        if (!quantity || Number(quantity) <= 0) {
            setFormError('Quantity must be positive');
            return;
        }
        if (orderType === 'LIMIT' && (!limitPrice || Number(limitPrice) <= 0)) {
            setFormError('Limit price is required for LIMIT orders');
            return;
        }

        try {
            setSubmitting(true);
            const orderData = {
                symbol: symbol.toUpperCase(),
                side,
                orderType,
                quantity: Number(quantity),
            };
            if (orderType === 'LIMIT') {
                orderData.limitPrice = Number(limitPrice);
            }

            const result = await placeOrder(token, orderData);
            setFormSuccess(
                `${result.side} ${result.requestedQty} ${result.symbol} — ${result.orderStatus}`
            );
            setSymbol('');
            setQuantity('');
            setLimitPrice('');
            loadOrders();
        } catch (err) {
            setFormError(err.message);
        } finally {
            setSubmitting(false);
        }
    };

    const handleCancel = async (orderId) => {
        try {
            await cancelOrder(token, orderId);
            loadOrders();
        } catch (err) {
            setError(err.message);
        }
    };

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

    const formatDate = (instant) => {
        if (!instant) return '—';
        return new Date(instant).toLocaleString('tr-TR', {
            day: '2-digit',
            month: '2-digit',
            year: 'numeric',
            hour: '2-digit',
            minute: '2-digit',
        });
    };

    const getStatusStyle = (status) => {
        switch (status) {
            case 'FILLED': return { color: '#16a34a', backgroundColor: '#f0fdf4' };
            case 'PENDING': return { color: '#d97706', backgroundColor: '#fffbeb' };
            case 'CANCELLED': return { color: '#dc2626', backgroundColor: '#fef2f2' };
            default: return { color: '#6b7280', backgroundColor: '#f9fafb' };
        }
    };

    const getSideStyle = (s) => {
        return s === 'BUY'
            ? { color: '#16a34a', fontWeight: '600' }
            : { color: '#dc2626', fontWeight: '600' };
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
                        <button style={styles.navLinkActive}>Orders</button>
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
                {/* ── Order Form ── */}
                <section style={styles.formCard}>
                    <h2 style={styles.formTitle}>Place Order</h2>

                    <div style={styles.formGrid}>
                        <div style={styles.fieldGroup}>
                            <label style={styles.label}>Symbol</label>
                            <input
                                type="text"
                                placeholder="e.g. THYAO"
                                value={symbol}
                                onChange={(e) => setSymbol(e.target.value)}
                                style={styles.input}
                                list="stock-symbols"
                            />
                            <datalist id="stock-symbols">
                                {stocks.map((s) => (
                                    <option key={s.symbol} value={s.symbol}>
                                        {s.companyName}
                                    </option>
                                ))}
                            </datalist>
                        </div>

                        <div style={styles.fieldGroup}>
                            <label style={styles.label}>Side</label>
                            <select
                                value={side}
                                onChange={(e) => setSide(e.target.value)}
                                style={styles.select}
                            >
                                <option value="BUY">BUY</option>
                                <option value="SELL">SELL</option>
                            </select>
                        </div>

                        <div style={styles.fieldGroup}>
                            <label style={styles.label}>Type</label>
                            <select
                                value={orderType}
                                onChange={(e) => setOrderType(e.target.value)}
                                style={styles.select}
                            >
                                <option value="MARKET">MARKET</option>
                                <option value="LIMIT">LIMIT</option>
                            </select>
                        </div>

                        <div style={styles.fieldGroup}>
                            <label style={styles.label}>Quantity</label>
                            <input
                                type="number"
                                placeholder="0"
                                min="1"
                                value={quantity}
                                onChange={(e) => setQuantity(e.target.value)}
                                style={styles.input}
                            />
                        </div>

                        {orderType === 'LIMIT' && (
                            <div style={styles.fieldGroup}>
                                <label style={styles.label}>Limit Price (₺)</label>
                                <input
                                    type="number"
                                    placeholder="0.00"
                                    min="0.01"
                                    step="0.01"
                                    value={limitPrice}
                                    onChange={(e) => setLimitPrice(e.target.value)}
                                    style={styles.input}
                                />
                            </div>
                        )}
                    </div>

                    <button
                        onClick={handleSubmit}
                        disabled={submitting}
                        style={{
                            ...styles.submitBtn,
                            ...(side === 'SELL' ? styles.submitBtnSell : {}),
                            opacity: submitting ? 0.6 : 1,
                        }}
                    >
                        {submitting ? 'Placing...' : `${side} Order`}
                    </button>

                    {formError && (
                        <div style={styles.formErrorBox}>
                            <p style={styles.formErrorText}>{formError}</p>
                        </div>
                    )}
                    {formSuccess && (
                        <div style={styles.formSuccessBox}>
                            <p style={styles.formSuccessText}>{formSuccess}</p>
                        </div>
                    )}
                </section>

                {/* ── Order History ── */}
                <section style={styles.historySection}>
                    <div style={styles.historyHeader}>
                        <h2 style={styles.historyTitle}>Order History</h2>
                        <select
                            value={statusFilter}
                            onChange={(e) => setStatusFilter(e.target.value)}
                            style={styles.filterSelect}
                            aria-label="Filter by status"
                        >
                            <option value="ALL">All</option>
                            <option value="PENDING">Pending</option>
                            <option value="FILLED">Filled</option>
                            <option value="CANCELLED">Cancelled</option>
                        </select>
                    </div>

                    {loading && (
                        <div style={styles.statusBox}>
                            <p style={styles.statusText}>Loading orders...</p>
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
                                        <th style={styles.th}>Date</th>
                                        <th style={styles.th}>Symbol</th>
                                        <th style={styles.th}>Side</th>
                                        <th style={styles.th}>Type</th>
                                        <th style={{ ...styles.th, textAlign: 'right' }}>Quantity</th>
                                        <th style={{ ...styles.th, textAlign: 'right' }}>Price</th>
                                        <th style={styles.th}>Status</th>
                                        <th style={styles.th}>Actions</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {orders.map((order) => (
                                        <tr key={order.id} style={styles.tr}>
                                            <td style={styles.td}>{formatDate(order.createdAt)}</td>
                                            <td style={styles.tdSymbol}>{order.symbol}</td>
                                            <td style={{ ...styles.td, ...getSideStyle(order.side) }}>
                                                {order.side}
                                            </td>
                                            <td style={styles.td}>{order.orderType}</td>
                                            <td style={{ ...styles.td, textAlign: 'right' }}>
                                                {order.requestedQty}
                                            </td>
                                            <td style={{ ...styles.td, textAlign: 'right' }}>
                                                {order.executionPrice
                                                    ? formatPrice(order.executionPrice)
                                                    : order.limitPrice
                                                        ? formatPrice(order.limitPrice)
                                                        : '—'}
                                            </td>
                                            <td style={styles.td}>
                                                <span style={{
                                                    ...styles.statusBadge,
                                                    ...getStatusStyle(order.orderStatus),
                                                }}>
                                                    {order.orderStatus}
                                                </span>
                                            </td>
                                            <td style={styles.td}>
                                                {order.orderStatus === 'PENDING' && (
                                                    <button
                                                        onClick={() => handleCancel(order.id)}
                                                        style={styles.cancelBtn}
                                                    >
                                                        Cancel
                                                    </button>
                                                )}
                                            </td>
                                        </tr>
                                    ))}
                                    {orders.length === 0 && (
                                        <tr>
                                            <td colSpan="8" style={styles.emptyRow}>
                                                No orders found
                                            </td>
                                        </tr>
                                    )}
                                </tbody>
                            </table>
                        </div>
                    )}
                </section>
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
        marginRight: '20px',
    },
    navLinks: {
        display: 'flex',
        gap: '4px',
    },
    navLink: {
        padding: '6px 14px',
        backgroundColor: 'transparent',
        color: 'rgba(255,255,255,0.6)',
        border: 'none',
        borderRadius: '6px',
        cursor: 'pointer',
        fontSize: '13px',
        fontWeight: '500',
    },
    navLinkActive: {
        padding: '6px 14px',
        backgroundColor: 'rgba(74,158,255,0.15)',
        color: '#4a9eff',
        border: 'none',
        borderRadius: '6px',
        cursor: 'default',
        fontSize: '13px',
        fontWeight: '600',
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
    formCard: {
        backgroundColor: '#ffffff',
        borderRadius: '10px',
        border: '1px solid #f0f0f0',
        padding: '24px',
        marginBottom: '28px',
    },
    formTitle: {
        fontSize: '18px',
        fontWeight: '700',
        color: '#111827',
        marginBottom: '20px',
    },
    formGrid: {
        display: 'grid',
        gridTemplateColumns: 'repeat(auto-fill, minmax(180px, 1fr))',
        gap: '16px',
        marginBottom: '20px',
    },
    fieldGroup: {
        display: 'flex',
        flexDirection: 'column',
        gap: '6px',
    },
    label: {
        fontSize: '12px',
        fontWeight: '600',
        color: '#6b7280',
        textTransform: 'uppercase',
        letterSpacing: '0.5px',
    },
    input: {
        padding: '10px 12px',
        fontSize: '14px',
        border: '1px solid #d1d5db',
        borderRadius: '8px',
        outline: 'none',
        backgroundColor: '#ffffff',
        boxSizing: 'border-box',
    },
    select: {
        padding: '10px 12px',
        fontSize: '14px',
        border: '1px solid #d1d5db',
        borderRadius: '8px',
        outline: 'none',
        backgroundColor: '#ffffff',
        boxSizing: 'border-box',
        cursor: 'pointer',
    },
    submitBtn: {
        padding: '10px 28px',
        backgroundColor: '#16a34a',
        color: '#ffffff',
        border: 'none',
        borderRadius: '8px',
        cursor: 'pointer',
        fontSize: '14px',
        fontWeight: '600',
    },
    submitBtnSell: {
        backgroundColor: '#dc2626',
    },
    formErrorBox: {
        marginTop: '12px',
        backgroundColor: '#fef2f2',
        borderRadius: '8px',
        padding: '10px 16px',
        border: '1px solid #fecaca',
    },
    formErrorText: {
        fontSize: '13px',
        color: '#dc2626',
        margin: 0,
    },
    formSuccessBox: {
        marginTop: '12px',
        backgroundColor: '#f0fdf4',
        borderRadius: '8px',
        padding: '10px 16px',
        border: '1px solid #bbf7d0',
    },
    formSuccessText: {
        fontSize: '13px',
        color: '#16a34a',
        margin: 0,
    },
    historySection: {
        marginTop: '8px',
    },
    historyHeader: {
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        marginBottom: '16px',
    },
    historyTitle: {
        fontSize: '18px',
        fontWeight: '700',
        color: '#111827',
    },
    filterSelect: {
        padding: '8px 12px',
        fontSize: '13px',
        border: '1px solid #d1d5db',
        borderRadius: '8px',
        outline: 'none',
        backgroundColor: '#ffffff',
        cursor: 'pointer',
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
    statusBadge: {
        padding: '3px 10px',
        borderRadius: '12px',
        fontSize: '11px',
        fontWeight: '600',
        letterSpacing: '0.3px',
    },
    cancelBtn: {
        padding: '4px 12px',
        backgroundColor: 'transparent',
        color: '#dc2626',
        border: '1px solid #fecaca',
        borderRadius: '6px',
        cursor: 'pointer',
        fontSize: '12px',
        fontWeight: '500',
    },
    emptyRow: {
        padding: '32px 16px',
        textAlign: 'center',
        color: '#9ca3af',
        fontSize: '14px',
    },
};