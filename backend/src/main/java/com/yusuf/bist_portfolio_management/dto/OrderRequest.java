package com.yusuf.bist_portfolio_management.dto;

import com.yusuf.bist_portfolio_management.enums.OrderSide;
import com.yusuf.bist_portfolio_management.enums.OrderType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderRequest {

    @NotBlank(message = "Symbol is required")
    private String symbol;

    @NotNull(message = "Order side is required")
    private OrderSide side;

    @NotNull(message = "Order type is required")
    private OrderType orderType;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    private BigDecimal quantity;

    private BigDecimal limitPrice;
}