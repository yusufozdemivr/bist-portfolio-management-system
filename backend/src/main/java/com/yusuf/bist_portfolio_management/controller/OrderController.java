package com.yusuf.bist_portfolio_management.controller;

import com.yusuf.bist_portfolio_management.dto.OrderRequest;
import com.yusuf.bist_portfolio_management.dto.OrderResponse;
import com.yusuf.bist_portfolio_management.enums.OrderStatus;
import com.yusuf.bist_portfolio_management.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid OrderRequest request) {

        OrderResponse response = orderService.placeOrder(
                userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getOrders(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) OrderStatus status) {

        List<OrderResponse> orders = orderService.getUserOrders(
                userDetails.getUsername(), status);
        return ResponseEntity.ok(orders);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<OrderResponse> cancelOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id) {

        OrderResponse response = orderService.cancelOrder(
                userDetails.getUsername(), id);
        return ResponseEntity.ok(response);
    }
}