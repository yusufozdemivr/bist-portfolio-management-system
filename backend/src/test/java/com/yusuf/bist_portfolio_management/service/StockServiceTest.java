package com.yusuf.bist_portfolio_management.service;

import com.yusuf.bist_portfolio_management.dto.StockResponse;
import com.yusuf.bist_portfolio_management.entity.Stock;
import com.yusuf.bist_portfolio_management.entity.StockLiveData;
import com.yusuf.bist_portfolio_management.exception.StockNotFoundException;
import com.yusuf.bist_portfolio_management.repository.StockRepository;
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
class StockServiceTest {

    @Mock
    private StockRepository stockRepository;

    @InjectMocks
    private StockService stockService;

    @Test
    void getAllStocks_returnsListOfStockResponses() {
        Stock stock = buildStockWithLiveData("THYAO", "Türk Hava Yolları",
                "Havacılık", new BigDecimal("323.50"),
                new BigDecimal("-1.07"));

        when(stockRepository.findByIsBist100TrueWithLiveData())
                .thenReturn(List.of(stock));

        List<StockResponse> result = stockService.getAllStocks();

        assertEquals(1, result.size());
        StockResponse response = result.get(0);
        assertEquals("THYAO", response.getSymbol());
        assertEquals("Türk Hava Yolları", response.getCompanyName());
        assertEquals("Havacılık", response.getSector());
        assertEquals(new BigDecimal("323.50"), response.getLastPrice());
        assertEquals(new BigDecimal("-1.07"),
                response.getChangePercentage());
    }

    @Test
    void getAllStocks_whenLiveDataIsNull_returnsNullPrices() {
        Stock stock = Stock.builder()
                .id(UUID.randomUUID())
                .symbol("SISE")
                .companyName("Şişecam")
                .sector("Cam")
                .isBist100(true)
                .build();

        when(stockRepository.findByIsBist100TrueWithLiveData())
                .thenReturn(List.of(stock));

        List<StockResponse> result = stockService.getAllStocks();

        assertEquals(1, result.size());
        StockResponse response = result.get(0);
        assertEquals("SISE", response.getSymbol());
        assertNull(response.getLastPrice());
        assertNull(response.getChangePercentage());
        assertNull(response.getVolume());
    }

    @Test
    void getStockBySymbol_found_returnsStockResponse() {
        Stock stock = buildStockWithLiveData("GARAN",
                "Garanti BBVA Bankası", "Bankacılık",
                new BigDecimal("145.20"), new BigDecimal("2.35"));

        when(stockRepository.findBySymbol("GARAN"))
                .thenReturn(Optional.of(stock));

        StockResponse response = stockService.getStockBySymbol("GARAN");

        assertEquals("GARAN", response.getSymbol());
        assertEquals(new BigDecimal("145.20"), response.getLastPrice());
    }

    @Test
    void getStockBySymbol_notFound_throwsException() {
        when(stockRepository.findBySymbol("XXXXX"))
                .thenReturn(Optional.empty());

        StockNotFoundException exception = assertThrows(
                StockNotFoundException.class,
                () -> stockService.getStockBySymbol("XXXXX"));

        assertTrue(exception.getMessage().contains("XXXXX"));
    }

    private Stock buildStockWithLiveData(String symbol, String companyName,
                                         String sector, BigDecimal lastPrice,
                                         BigDecimal changePercentage) {
        UUID stockId = UUID.randomUUID();
        Stock stock = Stock.builder()
                .id(stockId)
                .symbol(symbol)
                .companyName(companyName)
                .sector(sector)
                .isBist100(true)
                .build();

        StockLiveData liveData = StockLiveData.builder()
                .stockId(stockId)
                .stock(stock)
                .lastPrice(lastPrice)
                .changePercentage(changePercentage)
                .dayHigh(lastPrice.add(new BigDecimal("5.00")))
                .dayLow(lastPrice.subtract(new BigDecimal("3.00")))
                .bidPrice(lastPrice.subtract(new BigDecimal("0.10")))
                .askPrice(lastPrice.add(new BigDecimal("0.10")))
                .volume(10000000L)
                .updatedAt(Instant.now())
                .build();

        stock.setLiveData(liveData);
        return stock;
    }
}