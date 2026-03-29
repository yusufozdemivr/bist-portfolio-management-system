package com.yusuf.bist_portfolio_management.entity;

import com.yusuf.bist_portfolio_management.enums.OrderSide;
import com.yusuf.bist_portfolio_management.enums.OrderStatus;
import com.yusuf.bist_portfolio_management.enums.OrderType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "stock_order")
@NoArgsConstructor
@AllArgsConstructor
@Builder

@Getter
@Setter
@ToString(exclude = {"account", "stock", "executions"})
@EqualsAndHashCode(exclude = {"account", "stock", "executions"})

public class StockOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private TradingAccount account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", nullable = false, length = 10)
    private OrderType orderType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 5)
    private OrderSide side;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false, length = 20)
    @Builder.Default
    private OrderStatus orderStatus = OrderStatus.PENDING;

    @Column(name = "requested_qty", nullable = false, precision = 19, scale = 6)
    private BigDecimal requestedQty;

    @Column(name = "filled_qty", nullable = false, precision = 19, scale = 6)
    @Builder.Default
    private BigDecimal filledQty = BigDecimal.ZERO;

    // Nullable — MARKET emirlerde limit fiyat yok
    @Column(name = "limit_price", precision = 19, scale = 4)
    private BigDecimal limitPrice;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    @Builder.Default
    private List<TradeExecution> executions = new ArrayList<>();
}