package com.yusuf.bist_portfolio_management.entity;

import com.yusuf.bist_portfolio_management.enums.LedgerType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "account_ledger")
@NoArgsConstructor
@AllArgsConstructor
@Builder

@Getter
@Setter
@ToString(exclude = {"account", "tradeExecution"})
@EqualsAndHashCode(exclude = {"account", "tradeExecution"})

public class AccountLedger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private TradingAccount account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trade_execution_id")
    private TradeExecution tradeExecution;

    @Enumerated(EnumType.STRING)
    @Column(name = "ledger_type", nullable = false, length = 20)
    private LedgerType ledgerType;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(name = "balance_after", nullable = false, precision = 19, scale = 4)
    private BigDecimal balanceAfter;

    private String description;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}