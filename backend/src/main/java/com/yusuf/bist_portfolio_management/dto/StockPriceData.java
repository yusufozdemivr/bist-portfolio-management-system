package com.yusuf.bist_portfolio_management.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockPriceData {

    private String symbol;
    private BigDecimal lastPrice;
    private BigDecimal dayHigh;
    private BigDecimal dayLow;
    private Long volume;
    private BigDecimal changePercentage;
    private BigDecimal bidPrice;
    private BigDecimal askPrice;
    private BigDecimal dayLimitUp;
    private BigDecimal dayLimitDown;
    private BigDecimal turnover;
}
