package com.yusuf.bist_portfolio_management.controller;

import com.yusuf.bist_portfolio_management.dto.PortfolioSummaryResponse;
import com.yusuf.bist_portfolio_management.dto.PositionResponse;
import com.yusuf.bist_portfolio_management.dto.TransactionResponse;
import com.yusuf.bist_portfolio_management.service.PortfolioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/portfolio")
@RequiredArgsConstructor
public class PortfolioController {

    private final PortfolioService portfolioService;

    @GetMapping
    public ResponseEntity<List<PositionResponse>> getPortfolio(
            @AuthenticationPrincipal UserDetails userDetails) {

        List<PositionResponse> positions =
                portfolioService.getPortfolio(userDetails.getUsername());
        return ResponseEntity.ok(positions);
    }

    @GetMapping("/summary")
    public ResponseEntity<PortfolioSummaryResponse> getSummary(
            @AuthenticationPrincipal UserDetails userDetails) {

        PortfolioSummaryResponse summary =
                portfolioService.getSummary(userDetails.getUsername());
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/{symbol}/transactions")
    public ResponseEntity<List<TransactionResponse>> getTransactions(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String symbol) {

        List<TransactionResponse> transactions =
                portfolioService.getTransactions(userDetails.getUsername(), symbol);
        return ResponseEntity.ok(transactions);
    }
}