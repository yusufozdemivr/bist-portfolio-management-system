package com.yusuf.bist_portfolio_management.service;

import com.yusuf.bist_portfolio_management.dto.PortfolioSummaryResponse;
import com.yusuf.bist_portfolio_management.dto.PositionResponse;
import com.yusuf.bist_portfolio_management.dto.TransactionResponse;
import com.yusuf.bist_portfolio_management.entity.*;
import com.yusuf.bist_portfolio_management.enums.OrderSide;
import com.yusuf.bist_portfolio_management.exception.StockNotFoundException;
import com.yusuf.bist_portfolio_management.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PortfolioServiceTest {

    @Mock private AppUserRepository appUserRepository;
    @Mock private TradingAccountRepository tradingAccountRepository;
    @Mock private PortfolioPositionRepository portfolioPositionRepository;
    @Mock private TradeExecutionRepository tradeExecutionRepository;
    @Mock private StockRepository stockRepository;

    @InjectMocks
    private PortfolioService portfolioService;

    private AppUser testUser;
    private TradingAccount testAccount;

    @BeforeEach
    void setUp() {
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

        // Tüm service metotları resolveAccount çağırır
        when(appUserRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(testUser));
        when(tradingAccountRepository.findByUserId(testUser.getId()))
                .thenReturn(Optional.of(testAccount));
    }

    // ──────────────────────────────────────────────
    //  Test yardımcısı: pozisyon kur
    // ──────────────────────────────────────────────

    private PortfolioPosition buildPosition(
            String symbol, String company,
            String qty, String avgCost,
            String lastPrice, String changePct) {

        Stock stock = Stock.builder()
                .id(UUID.randomUUID())
                .symbol(symbol)
                .companyName(company)
                .build();

        StockLiveData live = StockLiveData.builder()
                .stock(stock)
                .lastPrice(lastPrice == null ? null : new BigDecimal(lastPrice))
                .changePercentage(changePct == null ? null : new BigDecimal(changePct))
                .build();
        stock.setLiveData(live);

        return PortfolioPosition.builder()
                .account(testAccount)
                .stock(stock)
                .currentQty(new BigDecimal(qty))
                .averageCost(new BigDecimal(avgCost))
                .build();
    }

    // ──────────────────────────────────────────────
    //  P&L — pozitif
    // ──────────────────────────────────────────────

    @Test
    void getPortfolio_positivePnl() {
        // avgCost 100, qty 10, price 120 → PnL +200, %20
        when(portfolioPositionRepository.findActivePositionsByAccountId(testAccount.getId()))
                .thenReturn(List.of(
                        buildPosition("THYAO", "Türk Hava Yolları",
                                "10", "100.0000", "120.0000", "5.0")));

        List<PositionResponse> result = portfolioService.getPortfolio("testuser");

        assertEquals(1, result.size());
        PositionResponse pos = result.get(0);
        assertEquals("THYAO", pos.getSymbol());
        assertEquals(0, new BigDecimal("1200").compareTo(pos.getMarketValue()));
        assertEquals(0, new BigDecimal("200").compareTo(pos.getUnrealizedPnl()));
        assertEquals(0, new BigDecimal("20").compareTo(pos.getPnlPercentage()));
    }

    // ──────────────────────────────────────────────
    //  P&L — negatif
    // ──────────────────────────────────────────────

    @Test
    void getPortfolio_negativePnl() {
        // avgCost 100, qty 10, price 80 → PnL -200, %-20
        when(portfolioPositionRepository.findActivePositionsByAccountId(testAccount.getId()))
                .thenReturn(List.of(
                        buildPosition("SISE", "Şişecam",
                                "10", "100.0000", "80.0000", "-3.0")));

        List<PositionResponse> result = portfolioService.getPortfolio("testuser");

        PositionResponse pos = result.get(0);
        assertEquals(0, new BigDecimal("800").compareTo(pos.getMarketValue()));
        assertEquals(0, new BigDecimal("-200").compareTo(pos.getUnrealizedPnl()));
        assertEquals(0, new BigDecimal("-20").compareTo(pos.getPnlPercentage()));
    }

    // ──────────────────────────────────────────────
    //  Null canlı fiyat → satırda null alanlar
    // ──────────────────────────────────────────────

    @Test
    void getPortfolio_nullLivePrice_returnsNullFields() {
        when(portfolioPositionRepository.findActivePositionsByAccountId(testAccount.getId()))
                .thenReturn(List.of(
                        buildPosition("XXXXX", "Fiyatsız A.Ş.",
                                "10", "100.0000", null, null)));

        List<PositionResponse> result = portfolioService.getPortfolio("testuser");

        PositionResponse pos = result.get(0);
        assertNull(pos.getCurrentPrice());
        assertNull(pos.getMarketValue());
        assertNull(pos.getUnrealizedPnl());
        assertNull(pos.getPnlPercentage());
        // maliyet ve adet yine de dolu
        assertEquals(0, new BigDecimal("100").compareTo(pos.getAverageCost()));
        assertEquals(0, new BigDecimal("10").compareTo(pos.getQuantity()));
    }

    // ──────────────────────────────────────────────
    //  Boş portföy
    // ──────────────────────────────────────────────

    @Test
    void getPortfolio_empty_returnsEmptyList() {
        when(portfolioPositionRepository.findActivePositionsByAccountId(testAccount.getId()))
                .thenReturn(List.of());

        List<PositionResponse> result = portfolioService.getPortfolio("testuser");

        assertTrue(result.isEmpty());
    }

    // ──────────────────────────────────────────────
    //  Summary — çok pozisyonlu toplulaştırma
    // ──────────────────────────────────────────────

    @Test
    void getSummary_aggregatesMultiplePositions() {
        // A: avg 100, qty 10, price 120, +%20 → mv 1200, cost 1000, daily 200
        // B: avg 50,  qty 20, price 55,  +%10 → mv 1100, cost 1000, daily 100
        when(portfolioPositionRepository.findActivePositionsByAccountId(testAccount.getId()))
                .thenReturn(List.of(
                        buildPosition("AAA", "A A.Ş.", "10", "100.0000", "120.0000", "20.0"),
                        buildPosition("BBB", "B A.Ş.", "20", "50.0000", "55.0000", "10.0")));

        PortfolioSummaryResponse s = portfolioService.getSummary("testuser");

        assertEquals(0, new BigDecimal("2000").compareTo(s.getTotalCostBasis()));
        assertEquals(0, new BigDecimal("2300").compareTo(s.getTotalMarketValue()));
        assertEquals(0, new BigDecimal("300").compareTo(s.getTotalUnrealizedPnl()));
        assertEquals(0, new BigDecimal("100000").compareTo(s.getCashBalance()));
        assertEquals(0, new BigDecimal("102300").compareTo(s.getTotalPortfolioValue()));
        assertEquals(0, new BigDecimal("300").compareTo(s.getDailyChange()));
        assertEquals(2, s.getPositionCount());
    }

    // ──────────────────────────────────────────────
    //  Summary — fiyatsız pozisyon toplama katılmaz, sayıda sayılır
    // ──────────────────────────────────────────────

    @Test
    void getSummary_skipsUnpricedButCountsIt() {
        when(portfolioPositionRepository.findActivePositionsByAccountId(testAccount.getId()))
                .thenReturn(List.of(
                        buildPosition("AAA", "A A.Ş.", "10", "100.0000", "120.0000", "20.0"),
                        buildPosition("XXX", "Fiyatsız", "5", "200.0000", null, null)));

        PortfolioSummaryResponse s = portfolioService.getSummary("testuser");

        // Sadece A toplama girer
        assertEquals(0, new BigDecimal("1000").compareTo(s.getTotalCostBasis()));
        assertEquals(0, new BigDecimal("1200").compareTo(s.getTotalMarketValue()));
        assertEquals(0, new BigDecimal("200").compareTo(s.getTotalUnrealizedPnl()));
        // Ama iki pozisyon da sayılır
        assertEquals(2, s.getPositionCount());
    }

    // ──────────────────────────────────────────────
    //  Transactions — execution eşleme
    // ──────────────────────────────────────────────

    @Test
    void getTransactions_mapsExecutions() {
        Stock stock = Stock.builder()
                .id(UUID.randomUUID())
                .symbol("THYAO")
                .companyName("Türk Hava Yolları")
                .build();

        StockOrder order = StockOrder.builder()
                .side(OrderSide.BUY)
                .build();

        TradeExecution exec = TradeExecution.builder()
                .order(order)
                .executionPrice(new BigDecimal("300.0000"))
                .executionQty(new BigDecimal("10"))
                .commissionFee(new BigDecimal("6.0000"))
                .totalAmount(new BigDecimal("3000.0000"))
                .executedAt(Instant.now())
                .build();

        when(stockRepository.findBySymbol("THYAO"))
                .thenReturn(Optional.of(stock));
        when(tradeExecutionRepository.findExecutionsByAccountAndSymbol(
                testAccount.getId(), "THYAO"))
                .thenReturn(List.of(exec));

        List<TransactionResponse> result =
                portfolioService.getTransactions("testuser", "thyao");

        assertEquals(1, result.size());
        TransactionResponse tx = result.get(0);
        assertEquals(OrderSide.BUY, tx.getSide());
        assertEquals(0, new BigDecimal("300").compareTo(tx.getPrice()));
        assertEquals(0, new BigDecimal("10").compareTo(tx.getQuantity()));
        assertEquals(0, new BigDecimal("6").compareTo(tx.getCommission()));
    }

    // ──────────────────────────────────────────────
    //  Transactions — hisse yoksa 404
    // ──────────────────────────────────────────────

    @Test
    void getTransactions_stockNotFound_throws() {
        when(stockRepository.findBySymbol("ZZZZZ"))
                .thenReturn(Optional.empty());

        assertThrows(StockNotFoundException.class,
                () -> portfolioService.getTransactions("testuser", "zzzzz"));
    }
}