package com.yusuf.bist_portfolio_management.dto;

import com.yusuf.bist_portfolio_management.enums.OrderSide;
import com.yusuf.bist_portfolio_management.enums.OrderStatus;
import com.yusuf.bist_portfolio_management.enums.OrderType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {

    private UUID id;
    private String symbol;
    private String companyName;
    private OrderSide side;
    private OrderType orderType;
    private OrderStatus orderStatus;
    private BigDecimal requestedQty;
    private BigDecimal filledQty;
    private BigDecimal limitPrice;
    private BigDecimal executionPrice;
    private BigDecimal commissionFee;
    private BigDecimal totalAmount;
    private Instant createdAt;
}