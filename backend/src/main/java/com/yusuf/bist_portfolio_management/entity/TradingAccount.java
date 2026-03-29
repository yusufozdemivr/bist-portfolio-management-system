package com.yusuf.bist_portfolio_management.entity;

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
@Table(name = "trading_account")
@NoArgsConstructor
@AllArgsConstructor
@Builder

@Getter
@Setter
@ToString(exclude ={"user", "ledgerEntries", "orders", "positions"})
@EqualsAndHashCode(exclude = {"user", "ledgerEntries", "orders", "positions"})

public class TradingAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private AppUser user;

    @Column(name = "cash_balance", nullable = false, precision = 19, scale = 4)
    private BigDecimal cashBalance;

    @Column(name = "blocked_cash", nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal blockedCash = BigDecimal.ZERO;

    @Column(name = "initial_balance", nullable = false, precision = 19, scale = 4)
    private BigDecimal initialBalance;

    @Version
    private Integer version;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;


    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL)
    @Builder.Default
    private List<AccountLedger> ledgerEntries = new ArrayList<>();

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL)
    @Builder.Default
    private List<StockOrder> orders = new ArrayList<>();

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL)
    @Builder.Default
    private List<PortfolioPosition> positions = new ArrayList<>();
}