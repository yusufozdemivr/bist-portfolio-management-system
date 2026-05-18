package com.yusuf.bist_portfolio_management.service;

import com.yusuf.bist_portfolio_management.dto.OrderRequest;
import com.yusuf.bist_portfolio_management.dto.OrderResponse;
import com.yusuf.bist_portfolio_management.entity.*;
import com.yusuf.bist_portfolio_management.enums.*;
import com.yusuf.bist_portfolio_management.exception.*;
import com.yusuf.bist_portfolio_management.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final StockOrderRepository stockOrderRepository;
    private final TradeExecutionRepository tradeExecutionRepository;
    private final AccountLedgerRepository accountLedgerRepository;
    private final PortfolioPositionRepository portfolioPositionRepository;
    private final TradingAccountRepository tradingAccountRepository;
    private final StockRepository stockRepository;
    private final AppUserRepository appUserRepository;

    @Value("${order.commission-rate}")
    private BigDecimal commissionRate;

    // ──────────────────────────────────────────────
    //  Place Order
    // ──────────────────────────────────────────────

    @Transactional
    public OrderResponse placeOrder(String username, OrderRequest request) {

        // 1) Kullanıcı ve hesap
        AppUser user = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new InvalidOrderException(
                        "User not found: " + username));

        TradingAccount account = tradingAccountRepository.findByUserId(user.getId())
                .orElseThrow(() -> new InvalidOrderException(
                        "Trading account not found for user: " + username));

        // 2) Hisse senedi
        Stock stock = stockRepository.findBySymbol(request.getSymbol().toUpperCase())
                .orElseThrow(() -> new StockNotFoundException(
                        "Stock not found: " + request.getSymbol().toUpperCase()));

        // 3) LIMIT emir validasyonu
        if (request.getOrderType() == OrderType.LIMIT && request.getLimitPrice() == null) {
            throw new InvalidOrderException(
                    "Limit price is required for LIMIT orders");
        }
        if (request.getOrderType() == OrderType.LIMIT
                && request.getLimitPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidOrderException(
                    "Limit price must be positive");
        }

        // 4) StockOrder oluştur
        StockOrder order = StockOrder.builder()
                .account(account)
                .stock(stock)
                .orderType(request.getOrderType())
                .side(request.getSide())
                .requestedQty(request.getQuantity())
                .limitPrice(request.getLimitPrice())
                .build();

        stockOrderRepository.save(order);

        // 5) MARKET → hemen execute et, LIMIT → PENDING kal
        if (request.getOrderType() == OrderType.MARKET) {
            executeOrder(order, account, stock);
        }

        return mapToResponse(order);
    }

    // ──────────────────────────────────────────────
    //  Execute Order (Market)
    // ──────────────────────────────────────────────

    private void executeOrder(StockOrder order, TradingAccount account, Stock stock) {

        // Güncel fiyat
        StockLiveData liveData = stock.getLiveData();
        if (liveData == null || liveData.getLastPrice() == null) {
            throw new InvalidOrderException(
                    "Live price not available for: " + stock.getSymbol());
        }

        BigDecimal price = liveData.getLastPrice();
        BigDecimal qty = order.getRequestedQty();
        BigDecimal totalAmount = price.multiply(qty).setScale(4, RoundingMode.HALF_UP);
        BigDecimal commission = totalAmount.multiply(commissionRate)
                .setScale(4, RoundingMode.HALF_UP);

        if (order.getSide() == OrderSide.BUY) {
            executeBuy(order, account, stock, price, qty, totalAmount, commission);
        } else {
            executeSell(order, account, stock, price, qty, totalAmount, commission);
        }
    }

    // ──────────────────────────────────────────────
    //  BUY Execution
    // ──────────────────────────────────────────────

    private void executeBuy(StockOrder order, TradingAccount account,
                            Stock stock, BigDecimal price, BigDecimal qty,
                            BigDecimal totalAmount, BigDecimal commission) {

        BigDecimal totalCost = totalAmount.add(commission);

        // Bakiye kontrolü
        if (account.getCashBalance().compareTo(totalCost) < 0) {
            throw new InsufficientBalanceException(
                    "Insufficient balance. Required: " + totalCost
                            + ", Available: " + account.getCashBalance());
        }

        // Bakiyeden düş
        account.setCashBalance(account.getCashBalance().subtract(totalCost));
        tradingAccountRepository.save(account);

        // Pozisyon oluştur veya güncelle
        PortfolioPosition position = portfolioPositionRepository
                .findByAccountIdAndStockId(account.getId(), stock.getId())
                .orElse(PortfolioPosition.builder()
                        .account(account)
                        .stock(stock)
                        .build());

        BigDecimal oldQty = position.getCurrentQty();
        BigDecimal oldAvgCost = position.getAverageCost();
        BigDecimal newQty = oldQty.add(qty);

        // Yeni ortalama maliyet hesapla
        BigDecimal newAvgCost = oldQty.multiply(oldAvgCost)
                .add(qty.multiply(price))
                .divide(newQty, 4, RoundingMode.HALF_UP);

        position.setCurrentQty(newQty);
        position.setAverageCost(newAvgCost);
        portfolioPositionRepository.save(position);

        // TradeExecution oluştur
        TradeExecution execution = TradeExecution.builder()
                .order(order)
                .executionPrice(price)
                .executionQty(qty)
                .commissionFee(commission)
                .totalAmount(totalAmount)
                .build();
        tradeExecutionRepository.save(execution);

        // Ledger kayıtları — BUY_EXEC
        BigDecimal balanceAfterBuy = account.getCashBalance().add(commission);
        AccountLedger buyLedger = AccountLedger.builder()
                .account(account)
                .tradeExecution(execution)
                .ledgerType(LedgerType.BUY_EXEC)
                .amount(totalAmount.negate())
                .balanceAfter(balanceAfterBuy)
                .description("BUY " + qty + " " + stock.getSymbol()
                        + " @ " + price)
                .build();
        accountLedgerRepository.save(buyLedger);

        // Ledger kayıtları — COMMISSION
        AccountLedger commissionLedger = AccountLedger.builder()
                .account(account)
                .tradeExecution(execution)
                .ledgerType(LedgerType.COMMISSION)
                .amount(commission.negate())
                .balanceAfter(account.getCashBalance())
                .description("Commission for BUY " + stock.getSymbol())
                .build();
        accountLedgerRepository.save(commissionLedger);

        // Order durumu güncelle
        order.setOrderStatus(OrderStatus.FILLED);
        order.setFilledQty(qty);
        stockOrderRepository.save(order);
    }

    // ──────────────────────────────────────────────
    //  SELL Execution
    // ──────────────────────────────────────────────

    private void executeSell(StockOrder order, TradingAccount account,
                             Stock stock, BigDecimal price, BigDecimal qty,
                             BigDecimal totalAmount, BigDecimal commission) {

        // Pozisyon kontrolü
        PortfolioPosition position = portfolioPositionRepository
                .findByAccountIdAndStockId(account.getId(), stock.getId())
                .orElseThrow(() -> new InsufficientPositionException(
                        "No position found for: " + stock.getSymbol()));

        if (position.getCurrentQty().compareTo(qty) < 0) {
            throw new InsufficientPositionException(
                    "Insufficient position. Available: " + position.getCurrentQty()
                            + ", Requested: " + qty);
        }

        BigDecimal netProceeds = totalAmount.subtract(commission);

        // Bakiyeye ekle
        account.setCashBalance(account.getCashBalance().add(netProceeds));
        tradingAccountRepository.save(account);

        // Realized PnL hesapla
        BigDecimal realizedPnl = qty.multiply(price.subtract(position.getAverageCost()))
                .setScale(4, RoundingMode.HALF_UP);
        position.setRealizedPnl(position.getRealizedPnl().add(realizedPnl));

        // Pozisyonu güncelle
        position.setCurrentQty(position.getCurrentQty().subtract(qty));
        portfolioPositionRepository.save(position);

        // TradeExecution oluştur
        TradeExecution execution = TradeExecution.builder()
                .order(order)
                .executionPrice(price)
                .executionQty(qty)
                .commissionFee(commission)
                .totalAmount(totalAmount)
                .build();
        tradeExecutionRepository.save(execution);

        // Ledger kayıtları — SELL_EXEC
        BigDecimal balanceAfterSell = account.getCashBalance().add(commission);
        AccountLedger sellLedger = AccountLedger.builder()
                .account(account)
                .tradeExecution(execution)
                .ledgerType(LedgerType.SELL_EXEC)
                .amount(totalAmount)
                .balanceAfter(balanceAfterSell)
                .description("SELL " + qty + " " + stock.getSymbol()
                        + " @ " + price)
                .build();
        accountLedgerRepository.save(sellLedger);

        // Ledger kayıtları — COMMISSION
        AccountLedger commissionLedger = AccountLedger.builder()
                .account(account)
                .tradeExecution(execution)
                .ledgerType(LedgerType.COMMISSION)
                .amount(commission.negate())
                .balanceAfter(account.getCashBalance())
                .description("Commission for SELL " + stock.getSymbol())
                .build();
        accountLedgerRepository.save(commissionLedger);

        // Order durumu güncelle
        order.setOrderStatus(OrderStatus.FILLED);
        order.setFilledQty(qty);
        stockOrderRepository.save(order);
    }

    // ──────────────────────────────────────────────
    //  Get User Orders
    // ──────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<OrderResponse> getUserOrders(String username, OrderStatus statusFilter) {

        AppUser user = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new InvalidOrderException(
                        "User not found: " + username));

        TradingAccount account = tradingAccountRepository.findByUserId(user.getId())
                .orElseThrow(() -> new InvalidOrderException(
                        "Trading account not found for user: " + username));

        List<StockOrder> orders;
        if (statusFilter == null) {
            orders = stockOrderRepository
                    .findByAccountIdOrderByCreatedAtDesc(account.getId());
        } else {
            orders = stockOrderRepository
                    .findByAccountIdAndOrderStatus(account.getId(), statusFilter);
        }

        return orders.stream()
                .map(this::mapToResponse)
                .toList();
    }

    // ──────────────────────────────────────────────
    //  Cancel Order
    // ──────────────────────────────────────────────

    @Transactional
    public OrderResponse cancelOrder(String username, UUID orderId) {

        AppUser user = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new InvalidOrderException(
                        "User not found: " + username));

        TradingAccount account = tradingAccountRepository.findByUserId(user.getId())
                .orElseThrow(() -> new InvalidOrderException(
                        "Trading account not found for user: " + username));

        StockOrder order = stockOrderRepository.findById(orderId)
                .orElseThrow(() -> new InvalidOrderException(
                        "Order not found: " + orderId));

        // Yetki kontrolü — sadece kendi emrini iptal edebilir
        if (!order.getAccount().getId().equals(account.getId())) {
            throw new InvalidOrderException(
                    "Order does not belong to current user");
        }

        // Sadece PENDING emirler iptal edilebilir
        if (order.getOrderStatus() != OrderStatus.PENDING) {
            throw new InvalidOrderException(
                    "Only PENDING orders can be cancelled. Current status: "
                            + order.getOrderStatus());
        }

        order.setOrderStatus(OrderStatus.CANCELLED);
        stockOrderRepository.save(order);

        return mapToResponse(order);
    }

    // ──────────────────────────────────────────────
    //  Mapper
    // ──────────────────────────────────────────────

    private OrderResponse mapToResponse(StockOrder order) {

        BigDecimal executionPrice = null;
        BigDecimal commissionFee = null;
        BigDecimal totalAmount = null;

        if (order.getOrderStatus() == OrderStatus.FILLED
                && !order.getExecutions().isEmpty()) {
            TradeExecution exec = order.getExecutions().get(0);
            executionPrice = exec.getExecutionPrice();
            commissionFee = exec.getCommissionFee();
            totalAmount = exec.getTotalAmount();
        }

        return OrderResponse.builder()
                .id(order.getId())
                .symbol(order.getStock().getSymbol())
                .companyName(order.getStock().getCompanyName())
                .side(order.getSide())
                .orderType(order.getOrderType())
                .orderStatus(order.getOrderStatus())
                .requestedQty(order.getRequestedQty())
                .filledQty(order.getFilledQty())
                .limitPrice(order.getLimitPrice())
                .executionPrice(executionPrice)
                .commissionFee(commissionFee)
                .totalAmount(totalAmount)
                .createdAt(order.getCreatedAt())
                .build();
    }
}