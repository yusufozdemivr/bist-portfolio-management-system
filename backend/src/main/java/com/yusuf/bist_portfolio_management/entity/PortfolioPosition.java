package com.yusuf.bist_portfolio_management.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "portfolio_position",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"account_id", "stock_id"}
        ))
@NoArgsConstructor
@AllArgsConstructor
@Builder

@Getter
@Setter
@ToString(exclude = {"account", "stock"})
@EqualsAndHashCode(exclude = {"account", "stock"})


public class PortfolioPosition {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private TradingAccount account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @Column(name = "current_qty", nullable = false, precision = 19, scale = 6)
    @Builder.Default
    private BigDecimal currentQty = BigDecimal.ZERO;

    @Column(name = "locked_qty", nullable = false, precision = 19, scale = 6)
    @Builder.Default
    private BigDecimal lockedQty = BigDecimal.ZERO;

    @Column(name = "average_cost", nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal averageCost = BigDecimal.ZERO;

    @Column(name = "realized_pnl", nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal realizedPnl = BigDecimal.ZERO;

    @Version
    private Integer version;

    @UpdateTimestamp
    @Column(name = "last_updated_at")
    private Instant lastUpdatedAt;
}