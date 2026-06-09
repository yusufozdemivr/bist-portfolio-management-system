package com.yusuf.bist_portfolio_management.service;

import com.yusuf.bist_portfolio_management.dto.PortfolioSummaryResponse;
import com.yusuf.bist_portfolio_management.dto.PositionResponse;
import com.yusuf.bist_portfolio_management.dto.TransactionResponse;
import com.yusuf.bist_portfolio_management.entity.AppUser;
import com.yusuf.bist_portfolio_management.entity.PortfolioPosition;
import com.yusuf.bist_portfolio_management.entity.StockLiveData;
import com.yusuf.bist_portfolio_management.entity.TradeExecution;
import com.yusuf.bist_portfolio_management.entity.TradingAccount;
import com.yusuf.bist_portfolio_management.exception.InvalidOrderException;
import com.yusuf.bist_portfolio_management.exception.StockNotFoundException;
import com.yusuf.bist_portfolio_management.repository.AppUserRepository;
import com.yusuf.bist_portfolio_management.repository.PortfolioPositionRepository;
import com.yusuf.bist_portfolio_management.repository.StockRepository;
import com.yusuf.bist_portfolio_management.repository.TradeExecutionRepository;
import com.yusuf.bist_portfolio_management.repository.TradingAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PortfolioService {

    private static final BigDecimal HUNDRED = new BigDecimal("100");
    private static final int MONEY_SCALE = 4;
    private static final int PCT_SCALE = 2;
    private static final int CALC_SCALE = 8;

    private final AppUserRepository appUserRepository;
    private final TradingAccountRepository tradingAccountRepository;
    private final PortfolioPositionRepository portfolioPositionRepository;
    private final TradeExecutionRepository tradeExecutionRepository;
    private final StockRepository stockRepository;

    // ──────────────────────────────────────────────
    //  Positions
    // ──────────────────────────────────────────────

    public List<PositionResponse> getPortfolio(String username) {
        TradingAccount account = resolveAccount(username);
        return portfolioPositionRepository
                .findActivePositionsByAccountId(account.getId())
                .stream()
                .map(this::mapToPositionResponse)
                .toList();
    }

    // ──────────────────────────────────────────────
    //  Summary
    // ──────────────────────────────────────────────

    public PortfolioSummaryResponse getSummary(String username) {
        TradingAccount account = resolveAccount(username);
        List<PortfolioPosition> positions =
                portfolioPositionRepository.findActivePositionsByAccountId(account.getId());

        BigDecimal totalCostBasis = BigDecimal.ZERO;
        BigDecimal totalMarketValue = BigDecimal.ZERO;
        BigDecimal dailyChange = BigDecimal.ZERO;

        for (PortfolioPosition p : positions) {
            BigDecimal qty = p.getCurrentQty();
            BigDecimal avgCost = p.getAverageCost();
            BigDecimal price = livePrice(p);

            // Canlı fiyatı olmayan pozisyon parasal toplulaştırmaya katılmaz
            if (price == null) {
                continue;
            }

            totalCostBasis = totalCostBasis.add(avgCost.multiply(qty));
            totalMarketValue = totalMarketValue.add(price.multiply(qty));

            BigDecimal prevClose = previousClose(p);
            if (prevClose != null) {
                dailyChange = dailyChange.add(qty.multiply(price.subtract(prevClose)));
            }
        }

        totalCostBasis = totalCostBasis.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
        totalMarketValue = totalMarketValue.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
        dailyChange = dailyChange.setScale(MONEY_SCALE, RoundingMode.HALF_UP);

        BigDecimal cash = account.getCashBalance();
        BigDecimal totalUnrealizedPnl = totalMarketValue.subtract(totalCostBasis);
        BigDecimal totalPortfolioValue = cash.add(totalMarketValue)
                .setScale(MONEY_SCALE, RoundingMode.HALF_UP);

        return PortfolioSummaryResponse.builder()
                .totalPortfolioValue(totalPortfolioValue)
                .totalCostBasis(totalCostBasis)
                .totalMarketValue(totalMarketValue)
                .totalUnrealizedPnl(totalUnrealizedPnl)
                .cashBalance(cash)
                .dailyChange(dailyChange)
                .positionCount(positions.size())
                .build();
    }

    // ──────────────────────────────────────────────
    //  Transactions (per stock)
    // ──────────────────────────────────────────────

    public List<TransactionResponse> getTransactions(String username, String symbol) {
        TradingAccount account = resolveAccount(username);
        String normalized = symbol.toUpperCase();

        stockRepository.findBySymbol(normalized)
                .orElseThrow(() -> new StockNotFoundException(
                        "Stock not found: " + normalized));

        return tradeExecutionRepository
                .findExecutionsByAccountAndSymbol(account.getId(), normalized)
                .stream()
                .map(this::mapToTransactionResponse)
                .toList();
    }

    // ──────────────────────────────────────────────
    //  Helpers
    // ──────────────────────────────────────────────

    private TradingAccount resolveAccount(String username) {
        AppUser user = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new InvalidOrderException(
                        "User not found: " + username));
        return tradingAccountRepository.findByUserId(user.getId())
                .orElseThrow(() -> new InvalidOrderException(
                        "Trading account not found for user: " + username));
    }

    private BigDecimal livePrice(PortfolioPosition p) {
        StockLiveData live = p.getStock().getLiveData();
        return (live == null) ? null : live.getLastPrice();
    }

    private BigDecimal previousClose(PortfolioPosition p) {
        StockLiveData live = p.getStock().getLiveData();
        if (live == null || live.getLastPrice() == null
                || live.getChangePercentage() == null) {
            return null;
        }
        // prevClose = lastPrice / (1 + changePct/100)
        BigDecimal factor = BigDecimal.ONE.add(
                live.getChangePercentage().divide(HUNDRED, CALC_SCALE, RoundingMode.HALF_UP));
        if (factor.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return live.getLastPrice().divide(factor, MONEY_SCALE, RoundingMode.HALF_UP);
    }

    private PositionResponse mapToPositionResponse(PortfolioPosition p) {
        BigDecimal qty = p.getCurrentQty();
        BigDecimal avgCost = p.getAverageCost();
        BigDecimal price = livePrice(p);

        BigDecimal marketValue = null;
        BigDecimal unrealizedPnl = null;
        BigDecimal pnlPercentage = null;

        if (price != null) {
            marketValue = price.multiply(qty)
                    .setScale(MONEY_SCALE, RoundingMode.HALF_UP);
            unrealizedPnl = price.subtract(avgCost).multiply(qty)
                    .setScale(MONEY_SCALE, RoundingMode.HALF_UP);
            if (avgCost.compareTo(BigDecimal.ZERO) > 0) {
                pnlPercentage = price.subtract(avgCost)
                        .divide(avgCost, CALC_SCALE, RoundingMode.HALF_UP)
                        .multiply(HUNDRED)
                        .setScale(PCT_SCALE, RoundingMode.HALF_UP);
            }
        }

        return PositionResponse.builder()
                .symbol(p.getStock().getSymbol())
                .companyName(p.getStock().getCompanyName())
                .quantity(qty)
                .averageCost(avgCost)
                .currentPrice(price)
                .marketValue(marketValue)
                .unrealizedPnl(unrealizedPnl)
                .pnlPercentage(pnlPercentage)
                .build();
    }

    private TransactionResponse mapToTransactionResponse(TradeExecution e) {
        return TransactionResponse.builder()
                .executedAt(e.getExecutedAt())
                .side(e.getOrder().getSide())
                .quantity(e.getExecutionQty())
                .price(e.getExecutionPrice())
                .commission(e.getCommissionFee())
                .totalAmount(e.getTotalAmount())
                .build();
    }
}