package com.yusuf.bist_portfolio_management.service;

import com.yusuf.bist_portfolio_management.dto.OrderRequest;
import com.yusuf.bist_portfolio_management.dto.OrderResponse;
import com.yusuf.bist_portfolio_management.entity.*;
import com.yusuf.bist_portfolio_management.enums.*;
import com.yusuf.bist_portfolio_management.exception.*;
import com.yusuf.bist_portfolio_management.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private StockOrderRepository stockOrderRepository;
    @Mock private TradeExecutionRepository tradeExecutionRepository;
    @Mock private AccountLedgerRepository accountLedgerRepository;
    @Mock private PortfolioPositionRepository portfolioPositionRepository;
    @Mock private TradingAccountRepository tradingAccountRepository;
    @Mock private StockRepository stockRepository;
    @Mock private AppUserRepository appUserRepository;

    @InjectMocks
    private OrderService orderService;

    private AppUser testUser;
    private TradingAccount testAccount;
    private Stock testStock;
    private StockLiveData testLiveData;

    @BeforeEach
    void setUp() throws Exception {
        // commissionRate alanını reflection ile set et
        var field = OrderService.class.getDeclaredField("commissionRate");
        field.setAccessible(true);
        field.set(orderService, new BigDecimal("0.002"));

        testUser = AppUser.builder()
                .id(UUID.randomUUID())
                .username("testuser")
                .build();

        testAccount = TradingAccount.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .cashBalance(new BigDecimal("100000.0000"))
                .initialBalance(new BigDecimal("100000.0000"))
                .build();

        testStock = Stock.builder()
                .id(UUID.randomUUID())
                .symbol("THYAO")
                .companyName("Türk Hava Yolları")
                .build();

        testLiveData = StockLiveData.builder()
                .stock(testStock)
                .lastPrice(new BigDecimal("300.0000"))
                .build();
        testStock.setLiveData(testLiveData);
    }

    // ──────────────────────────────────────────────
    //  Market BUY — başarılı
    // ──────────────────────────────────────────────

    @Test
    void placeOrder_marketBuy_success() {
        OrderRequest request = OrderRequest.builder()
                .symbol("THYAO")
                .side(OrderSide.BUY)
                .orderType(OrderType.MARKET)
                .quantity(new BigDecimal("10"))
                .build();

        when(appUserRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(testUser));
        when(tradingAccountRepository.findByUserId(testUser.getId()))
                .thenReturn(Optional.of(testAccount));
        when(stockRepository.findBySymbol("THYAO"))
                .thenReturn(Optional.of(testStock));
        when(portfolioPositionRepository.findByAccountIdAndStockId(
                testAccount.getId(), testStock.getId()))
                .thenReturn(Optional.empty());
        when(stockOrderRepository.save(any(StockOrder.class)))
                .thenAnswer(inv -> {
                    StockOrder o = inv.getArgument(0);
                    o.setId(UUID.randomUUID());
                    o.setExecutions(new ArrayList<>());
                    return o;
                });
        when(tradeExecutionRepository.save(any(TradeExecution.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(accountLedgerRepository.save(any(AccountLedger.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(portfolioPositionRepository.save(any(PortfolioPosition.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(tradingAccountRepository.save(any(TradingAccount.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        OrderResponse response = orderService.placeOrder("testuser", request);

        assertEquals(OrderStatus.FILLED, response.getOrderStatus());
        assertEquals("THYAO", response.getSymbol());

        // Bakiye kontrolü: 100000 - (10 * 300) - (3000 * 0.002) = 100000 - 3006 = 96994
        BigDecimal expectedBalance = new BigDecimal("100000.0000")
                .subtract(new BigDecimal("3000.0000"))
                .subtract(new BigDecimal("6.0000"));
        assertEquals(0, expectedBalance.compareTo(testAccount.getCashBalance()));

        verify(tradeExecutionRepository).save(any(TradeExecution.class));
        verify(accountLedgerRepository, times(2)).save(any(AccountLedger.class));
        verify(portfolioPositionRepository).save(any(PortfolioPosition.class));
    }

    // ──────────────────────────────────────────────
    //  Market BUY — yetersiz bakiye
    // ──────────────────────────────────────────────

    @Test
    void placeOrder_marketBuy_insufficientBalance() {
        testAccount.setCashBalance(new BigDecimal("100.0000"));

        OrderRequest request = OrderRequest.builder()
                .symbol("THYAO")
                .side(OrderSide.BUY)
                .orderType(OrderType.MARKET)
                .quantity(new BigDecimal("10"))
                .build();

        when(appUserRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(testUser));
        when(tradingAccountRepository.findByUserId(testUser.getId()))
                .thenReturn(Optional.of(testAccount));
        when(stockRepository.findBySymbol("THYAO"))
                .thenReturn(Optional.of(testStock));
        when(stockOrderRepository.save(any(StockOrder.class)))
                .thenAnswer(inv -> {
                    StockOrder o = inv.getArgument(0);
                    o.setId(UUID.randomUUID());
                    o.setExecutions(new ArrayList<>());
                    return o;
                });

        assertThrows(InsufficientBalanceException.class, () ->
                orderService.placeOrder("testuser", request));
    }

    // ──────────────────────────────────────────────
    //  Market SELL — başarılı
    // ──────────────────────────────────────────────

    @Test
    void placeOrder_marketSell_success() {
        PortfolioPosition position = PortfolioPosition.builder()
                .id(UUID.randomUUID())
                .account(testAccount)
                .stock(testStock)
                .currentQty(new BigDecimal("20"))
                .averageCost(new BigDecimal("250.0000"))
                .realizedPnl(BigDecimal.ZERO)
                .build();

        OrderRequest request = OrderRequest.builder()
                .symbol("THYAO")
                .side(OrderSide.SELL)
                .orderType(OrderType.MARKET)
                .quantity(new BigDecimal("10"))
                .build();

        when(appUserRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(testUser));
        when(tradingAccountRepository.findByUserId(testUser.getId()))
                .thenReturn(Optional.of(testAccount));
        when(stockRepository.findBySymbol("THYAO"))
                .thenReturn(Optional.of(testStock));
        when(portfolioPositionRepository.findByAccountIdAndStockId(
                testAccount.getId(), testStock.getId()))
                .thenReturn(Optional.of(position));
        when(stockOrderRepository.save(any(StockOrder.class)))
                .thenAnswer(inv -> {
                    StockOrder o = inv.getArgument(0);
                    o.setId(UUID.randomUUID());
                    o.setExecutions(new ArrayList<>());
                    return o;
                });
        when(tradeExecutionRepository.save(any(TradeExecution.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(accountLedgerRepository.save(any(AccountLedger.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(portfolioPositionRepository.save(any(PortfolioPosition.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(tradingAccountRepository.save(any(TradingAccount.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        OrderResponse response = orderService.placeOrder("testuser", request);

        assertEquals(OrderStatus.FILLED, response.getOrderStatus());

        // Pozisyon: 20 - 10 = 10
        assertEquals(0, new BigDecimal("10").compareTo(position.getCurrentQty()));

        // Realized PnL: 10 * (300 - 250) = 500
        assertEquals(0, new BigDecimal("500.0000").compareTo(position.getRealizedPnl()));

        // Bakiye: 100000 + 3000 - 6 = 102994
        BigDecimal expectedBalance = new BigDecimal("102994.0000");
        assertEquals(0, expectedBalance.compareTo(testAccount.getCashBalance()));
    }

    // ──────────────────────────────────────────────
    //  Market SELL — yetersiz pozisyon
    // ──────────────────────────────────────────────

    @Test
    void placeOrder_marketSell_insufficientPosition() {
        PortfolioPosition position = PortfolioPosition.builder()
                .id(UUID.randomUUID())
                .account(testAccount)
                .stock(testStock)
                .currentQty(new BigDecimal("5"))
                .averageCost(new BigDecimal("250.0000"))
                .realizedPnl(BigDecimal.ZERO)
                .build();

        OrderRequest request = OrderRequest.builder()
                .symbol("THYAO")
                .side(OrderSide.SELL)
                .orderType(OrderType.MARKET)
                .quantity(new BigDecimal("10"))
                .build();

        when(appUserRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(testUser));
        when(tradingAccountRepository.findByUserId(testUser.getId()))
                .thenReturn(Optional.of(testAccount));
        when(stockRepository.findBySymbol("THYAO"))
                .thenReturn(Optional.of(testStock));
        when(portfolioPositionRepository.findByAccountIdAndStockId(
                testAccount.getId(), testStock.getId()))
                .thenReturn(Optional.of(position));
        when(stockOrderRepository.save(any(StockOrder.class)))
                .thenAnswer(inv -> {
                    StockOrder o = inv.getArgument(0);
                    o.setId(UUID.randomUUID());
                    o.setExecutions(new ArrayList<>());
                    return o;
                });

        assertThrows(InsufficientPositionException.class, () ->
                orderService.placeOrder("testuser", request));
    }

    // ──────────────────────────────────────────────
    //  Market SELL — pozisyon yok
    // ──────────────────────────────────────────────

    @Test
    void placeOrder_marketSell_noPosition() {
        OrderRequest request = OrderRequest.builder()
                .symbol("THYAO")
                .side(OrderSide.SELL)
                .orderType(OrderType.MARKET)
                .quantity(new BigDecimal("10"))
                .build();

        when(appUserRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(testUser));
        when(tradingAccountRepository.findByUserId(testUser.getId()))
                .thenReturn(Optional.of(testAccount));
        when(stockRepository.findBySymbol("THYAO"))
                .thenReturn(Optional.of(testStock));
        when(portfolioPositionRepository.findByAccountIdAndStockId(
                testAccount.getId(), testStock.getId()))
                .thenReturn(Optional.empty());
        when(stockOrderRepository.save(any(StockOrder.class)))
                .thenAnswer(inv -> {
                    StockOrder o = inv.getArgument(0);
                    o.setId(UUID.randomUUID());
                    o.setExecutions(new ArrayList<>());
                    return o;
                });

        assertThrows(InsufficientPositionException.class, () ->
                orderService.placeOrder("testuser", request));
    }

    // ──────────────────────────────────────────────
    //  LIMIT emir — PENDING kalır
    // ──────────────────────────────────────────────

    @Test
    void placeOrder_limitOrder_staysPending() {
        OrderRequest request = OrderRequest.builder()
                .symbol("THYAO")
                .side(OrderSide.BUY)
                .orderType(OrderType.LIMIT)
                .quantity(new BigDecimal("10"))
                .limitPrice(new BigDecimal("280.0000"))
                .build();

        when(appUserRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(testUser));
        when(tradingAccountRepository.findByUserId(testUser.getId()))
                .thenReturn(Optional.of(testAccount));
        when(stockRepository.findBySymbol("THYAO"))
                .thenReturn(Optional.of(testStock));
        when(stockOrderRepository.save(any(StockOrder.class)))
                .thenAnswer(inv -> {
                    StockOrder o = inv.getArgument(0);
                    o.setId(UUID.randomUUID());
                    o.setExecutions(new ArrayList<>());
                    return o;
                });

        OrderResponse response = orderService.placeOrder("testuser", request);

        assertEquals(OrderStatus.PENDING, response.getOrderStatus());
        assertNull(response.getExecutionPrice());
        verify(tradeExecutionRepository, never()).save(any());
        verify(accountLedgerRepository, never()).save(any());
    }

    // ──────────────────────────────────────────────
    //  LIMIT emir — limitPrice null → hata
    // ──────────────────────────────────────────────

    @Test
    void placeOrder_limitOrder_noLimitPrice_throwsException() {
        OrderRequest request = OrderRequest.builder()
                .symbol("THYAO")
                .side(OrderSide.BUY)
                .orderType(OrderType.LIMIT)
                .quantity(new BigDecimal("10"))
                .limitPrice(null)
                .build();

        when(appUserRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(testUser));
        when(tradingAccountRepository.findByUserId(testUser.getId()))
                .thenReturn(Optional.of(testAccount));
        when(stockRepository.findBySymbol("THYAO"))
                .thenReturn(Optional.of(testStock));

        assertThrows(InvalidOrderException.class, () ->
                orderService.placeOrder("testuser", request));
    }

    // ──────────────────────────────────────────────
    //  Geçersiz symbol
    // ──────────────────────────────────────────────

    @Test
    void placeOrder_invalidSymbol_throwsException() {
        OrderRequest request = OrderRequest.builder()
                .symbol("INVALID")
                .side(OrderSide.BUY)
                .orderType(OrderType.MARKET)
                .quantity(new BigDecimal("10"))
                .build();

        when(appUserRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(testUser));
        when(tradingAccountRepository.findByUserId(testUser.getId()))
                .thenReturn(Optional.of(testAccount));
        when(stockRepository.findBySymbol("INVALID"))
                .thenReturn(Optional.empty());

        assertThrows(StockNotFoundException.class, () ->
                orderService.placeOrder("testuser", request));
    }

    // ──────────────────────────────────────────────
    //  Cancel PENDING emir — başarılı
    // ──────────────────────────────────────────────

    @Test
    void cancelOrder_pendingOrder_success() {
        UUID orderId = UUID.randomUUID();

        StockOrder order = StockOrder.builder()
                .id(orderId)
                .account(testAccount)
                .stock(testStock)
                .orderType(OrderType.LIMIT)
                .side(OrderSide.BUY)
                .orderStatus(OrderStatus.PENDING)
                .requestedQty(new BigDecimal("10"))
                .limitPrice(new BigDecimal("280.0000"))
                .build();
        order.setExecutions(new ArrayList<>());

        when(appUserRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(testUser));
        when(tradingAccountRepository.findByUserId(testUser.getId()))
                .thenReturn(Optional.of(testAccount));
        when(stockOrderRepository.findById(orderId))
                .thenReturn(Optional.of(order));
        when(stockOrderRepository.save(any(StockOrder.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        OrderResponse response = orderService.cancelOrder("testuser", orderId);

        assertEquals(OrderStatus.CANCELLED, response.getOrderStatus());
    }

    // ──────────────────────────────────────────────
    //  Cancel FILLED emir — hata
    // ──────────────────────────────────────────────

    @Test
    void cancelOrder_filledOrder_throwsException() {
        UUID orderId = UUID.randomUUID();

        StockOrder order = StockOrder.builder()
                .id(orderId)
                .account(testAccount)
                .stock(testStock)
                .orderType(OrderType.MARKET)
                .side(OrderSide.BUY)
                .orderStatus(OrderStatus.FILLED)
                .requestedQty(new BigDecimal("10"))
                .build();

        when(appUserRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(testUser));
        when(tradingAccountRepository.findByUserId(testUser.getId()))
                .thenReturn(Optional.of(testAccount));
        when(stockOrderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        assertThrows(InvalidOrderException.class, () ->
                orderService.cancelOrder("testuser", orderId));
    }

    // ──────────────────────────────────────────────
    //  Commission doğru hesaplanır
    // ──────────────────────────────────────────────

    @Test
    void placeOrder_marketBuy_commissionCalculatedCorrectly() {
        OrderRequest request = OrderRequest.builder()
                .symbol("THYAO")
                .side(OrderSide.BUY)
                .orderType(OrderType.MARKET)
                .quantity(new BigDecimal("5"))
                .build();

        when(appUserRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(testUser));
        when(tradingAccountRepository.findByUserId(testUser.getId()))
                .thenReturn(Optional.of(testAccount));
        when(stockRepository.findBySymbol("THYAO"))
                .thenReturn(Optional.of(testStock));
        when(portfolioPositionRepository.findByAccountIdAndStockId(
                testAccount.getId(), testStock.getId()))
                .thenReturn(Optional.empty());
        when(stockOrderRepository.save(any(StockOrder.class)))
                .thenAnswer(inv -> {
                    StockOrder o = inv.getArgument(0);
                    o.setId(UUID.randomUUID());
                    o.setExecutions(new ArrayList<>());
                    return o;
                });
        when(tradeExecutionRepository.save(any(TradeExecution.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(accountLedgerRepository.save(any(AccountLedger.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(portfolioPositionRepository.save(any(PortfolioPosition.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(tradingAccountRepository.save(any(TradingAccount.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        orderService.placeOrder("testuser", request);

        // Commission: 5 * 300 * 0.002 = 3.0000
        ArgumentCaptor<TradeExecution> captor =
                ArgumentCaptor.forClass(TradeExecution.class);
        verify(tradeExecutionRepository).save(captor.capture());

        BigDecimal expectedCommission = new BigDecimal("3.0000");
        assertEquals(0, expectedCommission.compareTo(
                captor.getValue().getCommissionFee()));
    }
}