package com.yusuf.bist_portfolio_management.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockResponse {

    private String symbol;
    private String companyName;
    private String sector;
    private BigDecimal lastPrice;
    private BigDecimal changePercentage;
    private BigDecimal dayHigh;
    private BigDecimal dayLow;
    private BigDecimal bidPrice;
    private BigDecimal askPrice;
    private BigDecimal dayLimitUp;
    private BigDecimal dayLimitDown;
    private Long volume;
    private Instant updatedAt;
}