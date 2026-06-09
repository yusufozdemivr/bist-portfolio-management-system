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
public class PortfolioSummaryResponse {

    private BigDecimal totalPortfolioValue;
    private BigDecimal totalCostBasis;
    private BigDecimal totalMarketValue;
    private BigDecimal totalUnrealizedPnl;
    private BigDecimal cashBalance;
    private BigDecimal dailyChange;
    private int positionCount;
}