package com.yusuf.bist_portfolio_management.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "stock_live_data")
@NoArgsConstructor
@AllArgsConstructor
@Builder

@Getter
@Setter
@ToString(exclude ={"stock"})
@EqualsAndHashCode(exclude = {"stock"})

public class StockLiveData {

    @Id
    private UUID stockId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "stock_id")
    private Stock stock;

    @Column(name = "last_price", precision = 19, scale = 4)
    private BigDecimal lastPrice;

    // BigPara "alis" alanı
    @Column(name = "bid_price", precision = 19, scale = 4)
    private BigDecimal bidPrice;

    // BigPara "satis" alanı
    @Column(name = "ask_price", precision = 19, scale = 4)
    private BigDecimal askPrice;

    @Column(name = "change_percentage", precision = 19, scale = 4)
    private BigDecimal changePercentage;

    @Column(name = "day_high", precision = 19, scale = 4)
    private BigDecimal dayHigh;

    @Column(name = "day_low", precision = 19, scale = 4)
    private BigDecimal dayLow;

    // BigPara "tavan" alanı — emir validasyonu için
    @Column(name = "day_limit_up", precision = 19, scale = 4)
    private BigDecimal dayLimitUp;

    // BigPara "taban" alanı — emir validasyonu için
    @Column(name = "day_limit_down", precision = 19, scale = 4)
    private BigDecimal dayLimitDown;

    private Long volume;

    @Column(precision = 19, scale = 4)
    private BigDecimal turnover;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}