package com.yusuf.bist_portfolio_management.dto;

import com.yusuf.bist_portfolio_management.enums.OrderSide;
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
public class TransactionResponse {

    private Instant executedAt;
    private OrderSide side;
    private BigDecimal quantity;
    private BigDecimal price;
    private BigDecimal commission;
    private BigDecimal totalAmount;
}