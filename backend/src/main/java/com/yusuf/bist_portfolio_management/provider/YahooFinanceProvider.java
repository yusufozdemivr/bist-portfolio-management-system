package com.yusuf.bist_portfolio_management.provider;

import com.yusuf.bist_portfolio_management.dto.StockPriceData;
import com.yusuf.bist_portfolio_management.dto.YahooFinanceResponse;
import com.yusuf.bist_portfolio_management.entity.Stock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Component
@Primary
@RequiredArgsConstructor
@Slf4j
public class YahooFinanceProvider implements StockDataProvider {

    private final RestTemplate restTemplate;

    @Value("${yahoo.finance.base-url}")
    private String baseUrl;

    @Value("${scheduler.stock-price.request-delay-ms}")
    private long requestDelayMs;

    private static final BigDecimal BIST_LIMIT_PERCENTAGE = new BigDecimal("0.10");
    private static final BigDecimal ONE = BigDecimal.ONE;

    @Override
    public List<StockPriceData> fetchPrices(List<Stock> stocks) {
        List<StockPriceData> results = new ArrayList<>();

        for (Stock stock : stocks) {
            try {
                String url = baseUrl + "/" + stock.getSymbol()
                        + ".IS?interval=1d&range=1d";

                YahooFinanceResponse response = restTemplate
                        .getForObject(url, YahooFinanceResponse.class);

                if (response == null
                        || response.getChart() == null
                        || response.getChart().getResult() == null
                        || response.getChart().getResult().isEmpty()) {
                    log.warn("Empty response for symbol: {}",
                            stock.getSymbol());
                    continue;
                }

                YahooFinanceResponse.Meta meta = response.getChart()
                        .getResult().get(0).getMeta();

                BigDecimal previousClose = meta.getChartPreviousClose();
                BigDecimal changePercentage = calculateChangePercentage(
                        meta.getRegularMarketPrice(), previousClose);
                BigDecimal dayLimitUp = calculateLimitUp(previousClose);
                BigDecimal dayLimitDown = calculateLimitDown(previousClose);

                StockPriceData priceData = StockPriceData.builder()
                        .symbol(stock.getSymbol())
                        .lastPrice(meta.getRegularMarketPrice())
                        .dayHigh(meta.getRegularMarketDayHigh())
                        .dayLow(meta.getRegularMarketDayLow())
                        .volume(meta.getRegularMarketVolume())
                        .changePercentage(changePercentage)
                        .dayLimitUp(dayLimitUp)
                        .dayLimitDown(dayLimitDown)
                        .build();

                results.add(priceData);

                Thread.sleep(requestDelayMs);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Thread interrupted while fetching prices");
                break;
            } catch (Exception e) {
                log.error("Failed to fetch price for {}: {}",
                        stock.getSymbol(), e.getMessage());
            }
        }

        log.info("Yahoo Finance: fetched {}/{} stock prices",
                results.size(), stocks.size());
        return results;
    }

    private BigDecimal calculateChangePercentage(
            BigDecimal currentPrice, BigDecimal previousClose) {
        if (previousClose == null
                || previousClose.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return currentPrice.subtract(previousClose)
                .divide(previousClose, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
    }

    private BigDecimal calculateLimitUp(BigDecimal previousClose) {
        if (previousClose == null) {
            return null;
        }
        return previousClose.multiply(ONE.add(BIST_LIMIT_PERCENTAGE))
                .setScale(4, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateLimitDown(BigDecimal previousClose) {
        if (previousClose == null) {
            return null;
        }
        return previousClose.multiply(ONE.subtract(BIST_LIMIT_PERCENTAGE))
                .setScale(4, RoundingMode.HALF_UP);
    }
}