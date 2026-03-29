package com.yusuf.bist_portfolio_management.entity;


import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

@Entity
@Table(name = "stock")
@NoArgsConstructor
@AllArgsConstructor
@Builder

@Getter
@Setter
@ToString(exclude ={"liveData", "priceHistory", "orders", "positions"})
@EqualsAndHashCode(exclude ={"liveData", "priceHistory", "orders", "positions"})

public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 10)
    private String symbol;

    @Column(name = "company_name", nullable = false, length = 100)
    private String companyName;

    @Column(name = "isin_code", length = 12)
    private String isinCode;

    @Column(length = 50)
    private String sector;

    @Column(name = "is_active_trading", nullable = false)
    @Builder.Default
    private Boolean isActiveTrading = true;

    @Column(name = "is_bist100", nullable = false)
    @Builder.Default
    private Boolean isBist100 = false;


    @OneToOne(mappedBy = "stock", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private StockLiveData liveData;

    @OneToMany(mappedBy = "stock", cascade = CascadeType.ALL)
    @Builder.Default
    private List<StockPriceHistory> priceHistory = new ArrayList<>();

    @OneToMany(mappedBy = "stock")
    @Builder.Default
    private List<StockOrder> orders = new ArrayList<>();

    @OneToMany(mappedBy = "stock")
    @Builder.Default
    private List<PortfolioPosition> positions = new ArrayList<>();
}
