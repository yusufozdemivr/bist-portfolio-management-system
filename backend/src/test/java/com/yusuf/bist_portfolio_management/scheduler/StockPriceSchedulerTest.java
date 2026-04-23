package com.yusuf.bist_portfolio_management.scheduler;

import com.yusuf.bist_portfolio_management.dto.StockPriceData;
import com.yusuf.bist_portfolio_management.entity.Stock;
import com.yusuf.bist_portfolio_management.entity.StockLiveData;
import com.yusuf.bist_portfolio_management.provider.StockDataProvider;
import com.yusuf.bist_portfolio_management.repository.StockLiveDataRepository;
import com.yusuf.bist_portfolio_management.repository.StockRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockPriceSchedulerTest {

    @Mock
    private StockDataProvider stockDataProvider;

    @Mock
    private StockRepository stockRepository;

    @Mock
    private StockLiveDataRepository stockLiveDataRepository;

    @InjectMocks
    private StockPriceScheduler stockPriceScheduler;

    @Test
    void fetchStockPrices_CreatesNewLiveDataWhenNotExists() {
        UUID stockId = UUID.randomUUID();
        Stock stock = Stock.builder()
                .id(stockId)
                .symbol("THYAO")
                .companyName("Türk Hava Yolları")
                .isBist100(true)
                .build();

        StockPriceData priceData = StockPriceData.builder()
                .symbol("THYAO")
                .lastPrice(new BigDecimal("323.50"))
                .dayHigh(new BigDecimal("331.25"))
                .dayLow(new BigDecimal("319.50"))
                .volume(37829627L)
                .changePercentage(new BigDecimal("-1.0703"))
                .dayLimitUp(new BigDecimal("359.7000"))
                .dayLimitDown(new BigDecimal("294.3000"))
                .build();

        when(stockRepository.findByIsBist100True())
                .thenReturn(List.of(stock));
        when(stockDataProvider.fetchPrices(List.of(stock)))
                .thenReturn(List.of(priceData));
        when(stockLiveDataRepository.findById(stockId))
                .thenReturn(Optional.empty());

        stockPriceScheduler.fetchStockPrices();

        ArgumentCaptor<StockLiveData> captor =
                ArgumentCaptor.forClass(StockLiveData.class);
        verify(stockLiveDataRepository).save(captor.capture());

        StockLiveData saved = captor.getValue();
        assertEquals(stock, saved.getStock());
        assertEquals(new BigDecimal("323.50"), saved.getLastPrice());
        assertEquals(new BigDecimal("331.25"), saved.getDayHigh());
        assertEquals(new BigDecimal("319.50"), saved.getDayLow());
        assertEquals(37829627L, saved.getVolume());
        assertEquals(new BigDecimal("359.7000"),
                saved.getDayLimitUp());
        assertEquals(new BigDecimal("294.3000"),
                saved.getDayLimitDown());
    }

    @Test
    void fetchStockPrices_UpdatesExistingLiveData() {
        UUID stockId = UUID.randomUUID();
        Stock stock = Stock.builder()
                .id(stockId)
                .symbol("SISE")
                .companyName("Şişecam")
                .isBist100(true)
                .build();

        StockLiveData existingData = new StockLiveData();
        existingData.setStock(stock);
        existingData.setLastPrice(new BigDecimal("46.00"));

        StockPriceData priceData = StockPriceData.builder()
                .symbol("SISE")
                .lastPrice(new BigDecimal("47.40"))
                .dayHigh(new BigDecimal("47.84"))
                .dayLow(new BigDecimal("46.56"))
                .volume(35252373L)
                .changePercentage(new BigDecimal("0.38"))
                .build();

        when(stockRepository.findByIsBist100True())
                .thenReturn(List.of(stock));
        when(stockDataProvider.fetchPrices(List.of(stock)))
                .thenReturn(List.of(priceData));
        when(stockLiveDataRepository.findById(stockId))
                .thenReturn(Optional.of(existingData));

        stockPriceScheduler.fetchStockPrices();

        ArgumentCaptor<StockLiveData> captor =
                ArgumentCaptor.forClass(StockLiveData.class);
        verify(stockLiveDataRepository).save(captor.capture());

        StockLiveData saved = captor.getValue();
        assertEquals(new BigDecimal("47.40"), saved.getLastPrice());
        assertEquals(new BigDecimal("47.84"), saved.getDayHigh());
    }

    @Test
    void fetchStockPrices_WhenNoBist100Stocks_DoesNotCallProvider() {
        when(stockRepository.findByIsBist100True())
                .thenReturn(List.of());

        stockPriceScheduler.fetchStockPrices();

        verify(stockDataProvider, never()).fetchPrices(any());
        verify(stockLiveDataRepository, never()).save(any());
    }

    @Test
    void fetchStockPrices_WhenProviderReturnsEmpty_DoesNotSave() {
        Stock stock = Stock.builder()
                .id(UUID.randomUUID())
                .symbol("THYAO")
                .companyName("Türk Hava Yolları")
                .isBist100(true)
                .build();

        when(stockRepository.findByIsBist100True())
                .thenReturn(List.of(stock));
        when(stockDataProvider.fetchPrices(List.of(stock)))
                .thenReturn(List.of());

        stockPriceScheduler.fetchStockPrices();

        verify(stockLiveDataRepository, never()).save(any());
    }
}