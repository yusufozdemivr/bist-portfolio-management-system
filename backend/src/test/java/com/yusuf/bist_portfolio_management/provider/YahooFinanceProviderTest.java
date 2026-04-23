package com.yusuf.bist_portfolio_management.provider;

import com.yusuf.bist_portfolio_management.dto.StockPriceData;
import com.yusuf.bist_portfolio_management.dto.YahooFinanceResponse;
import com.yusuf.bist_portfolio_management.entity.Stock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class YahooFinanceProviderTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private YahooFinanceProvider yahooFinanceProvider;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(yahooFinanceProvider,
                "baseUrl", "https://query1.finance.yahoo.com/v8/finance/chart");
        ReflectionTestUtils.setField(yahooFinanceProvider,
                "requestDelayMs", 0L);
    }

    @Test
    void fetchPrices_WithValidResponse_MapsFieldsCorrectly() {
        Stock stock = Stock.builder()
                .id(UUID.randomUUID())
                .symbol("THYAO")
                .companyName("Türk Hava Yolları")
                .isBist100(true)
                .build();

        YahooFinanceResponse response = buildYahooResponse(
                "THYAO.IS",
                new BigDecimal("323.50"),
                new BigDecimal("331.25"),
                new BigDecimal("319.50"),
                37829627L,
                new BigDecimal("327.00")
        );

        when(restTemplate.getForObject(anyString(),
                eq(YahooFinanceResponse.class)))
                .thenReturn(response);

        List<StockPriceData> result = yahooFinanceProvider
                .fetchPrices(List.of(stock));

        assertEquals(1, result.size());

        StockPriceData data = result.get(0);
        assertEquals("THYAO", data.getSymbol());
        assertEquals(new BigDecimal("323.50"), data.getLastPrice());
        assertEquals(new BigDecimal("331.25"), data.getDayHigh());
        assertEquals(new BigDecimal("319.50"), data.getDayLow());
        assertEquals(37829627L, data.getVolume());
        assertNull(data.getBidPrice());
        assertNull(data.getAskPrice());
    }

    @Test
    void fetchPrices_CalculatesChangePercentageCorrectly() {
        Stock stock = Stock.builder()
                .id(UUID.randomUUID())
                .symbol("SISE")
                .companyName("Şişecam")
                .isBist100(true)
                .build();

        YahooFinanceResponse response = buildYahooResponse(
                "SISE.IS",
                new BigDecimal("110.00"),
                new BigDecimal("115.00"),
                new BigDecimal("105.00"),
                1000000L,
                new BigDecimal("100.00")
        );

        when(restTemplate.getForObject(anyString(),
                eq(YahooFinanceResponse.class)))
                .thenReturn(response);

        List<StockPriceData> result = yahooFinanceProvider
                .fetchPrices(List.of(stock));

        StockPriceData data = result.get(0);
        assertEquals(0,
                new BigDecimal("10.0000")
                        .compareTo(data.getChangePercentage()));
    }

    @Test
    void fetchPrices_CalculatesLimitUpAndDownCorrectly() {
        Stock stock = Stock.builder()
                .id(UUID.randomUUID())
                .symbol("GARAN")
                .companyName("Garanti BBVA")
                .isBist100(true)
                .build();

        BigDecimal previousClose = new BigDecimal("100.00");

        YahooFinanceResponse response = buildYahooResponse(
                "GARAN.IS",
                new BigDecimal("102.00"),
                new BigDecimal("103.00"),
                new BigDecimal("99.00"),
                5000000L,
                previousClose
        );

        when(restTemplate.getForObject(anyString(),
                eq(YahooFinanceResponse.class)))
                .thenReturn(response);

        List<StockPriceData> result = yahooFinanceProvider
                .fetchPrices(List.of(stock));

        StockPriceData data = result.get(0);
        assertEquals(0,
                new BigDecimal("110.0000")
                        .compareTo(data.getDayLimitUp()));
        assertEquals(0,
                new BigDecimal("90.0000")
                        .compareTo(data.getDayLimitDown()));
    }

    @Test
    void fetchPrices_WhenApiFails_ReturnsEmptyAndContinues() {
        Stock stock1 = Stock.builder()
                .id(UUID.randomUUID())
                .symbol("FAIL")
                .companyName("Fail Stock")
                .isBist100(true)
                .build();

        Stock stock2 = Stock.builder()
                .id(UUID.randomUUID())
                .symbol("SISE")
                .companyName("Şişecam")
                .isBist100(true)
                .build();

        YahooFinanceResponse validResponse = buildYahooResponse(
                "SISE.IS",
                new BigDecimal("47.40"),
                new BigDecimal("47.84"),
                new BigDecimal("46.56"),
                35252373L,
                new BigDecimal("47.22")
        );

        when(restTemplate.getForObject(
                eq("https://query1.finance.yahoo.com/v8/finance/chart/FAIL.IS?interval=1d&range=1d"),
                eq(YahooFinanceResponse.class)))
                .thenThrow(new RuntimeException("Connection refused"));

        when(restTemplate.getForObject(
                eq("https://query1.finance.yahoo.com/v8/finance/chart/SISE.IS?interval=1d&range=1d"),
                eq(YahooFinanceResponse.class)))
                .thenReturn(validResponse);

        List<StockPriceData> result = yahooFinanceProvider
                .fetchPrices(List.of(stock1, stock2));

        assertEquals(1, result.size());
        assertEquals("SISE", result.get(0).getSymbol());
    }

    private YahooFinanceResponse buildYahooResponse(
            String symbol, BigDecimal price, BigDecimal high,
            BigDecimal low, Long volume, BigDecimal previousClose) {

        YahooFinanceResponse.Meta meta = new YahooFinanceResponse.Meta();
        meta.setSymbol(symbol);
        meta.setRegularMarketPrice(price);
        meta.setRegularMarketDayHigh(high);
        meta.setRegularMarketDayLow(low);
        meta.setRegularMarketVolume(volume);
        meta.setChartPreviousClose(previousClose);

        YahooFinanceResponse.Result result = new YahooFinanceResponse.Result();
        result.setMeta(meta);

        YahooFinanceResponse.Chart chart = new YahooFinanceResponse.Chart();
        chart.setResult(List.of(result));

        YahooFinanceResponse response = new YahooFinanceResponse();
        response.setChart(chart);
        return response;
    }
}