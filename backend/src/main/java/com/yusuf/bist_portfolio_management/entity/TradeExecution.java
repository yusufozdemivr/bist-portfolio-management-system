package com.yusuf.bist_portfolio_management.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "trade_execution")
@NoArgsConstructor
@AllArgsConstructor
@Builder

@Getter
@Setter
@ToString(exclude = {"order", "ledgerEntries"})
@EqualsAndHashCode(exclude = {"order", "ledgerEntries"})


public class TradeExecution {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private StockOrder order;

    @Column(name = "execution_price", nullable = false, precision = 19, scale = 4)
    private BigDecimal executionPrice;

    @Column(name = "execution_qty", nullable = false, precision = 19, scale = 6)
    private BigDecimal executionQty;

    @Column(name = "commission_fee", nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal commissionFee = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalAmount;

    @CreationTimestamp
    @Column(name = "executed_at", nullable = false, updatable = false)
    private Instant executedAt;

    @OneToMany(mappedBy = "tradeExecution", cascade = CascadeType.ALL)
    @Builder.Default
    private List<AccountLedger> ledgerEntries = new ArrayList<>();
}