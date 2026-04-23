package com.yusuf.bist_portfolio_management.provider;

import com.yusuf.bist_portfolio_management.dto.BigParaResponse;
import com.yusuf.bist_portfolio_management.dto.StockPriceData;
import com.yusuf.bist_portfolio_management.entity.Stock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class BigParaProvider implements StockDataProvider {

    private final RestTemplate restTemplate;

    @Value("${bigpara.base-url}")
    private String baseUrl;

    @Value("${scheduler.stock-price.request-delay-ms}")
    private long requestDelayMs;

    @Override
    public List<StockPriceData> fetchPrices(List<Stock> stocks) {
        List<StockPriceData> results = new ArrayList<>();

        for (Stock stock : stocks) {
            try {
                String url = baseUrl + "/" + stock.getSymbol();

                BigParaResponse response = restTemplate
                        .getForObject(url, BigParaResponse.class);

                if (response == null
                        || !"0".equals(response.getCode())
                        || response.getData() == null
                        || response.getData().getHisseYuzeysel() == null) {
                    log.warn("Invalid response for symbol: {}",
                            stock.getSymbol());
                    continue;
                }

                BigParaResponse.HisseYuzeysel hisse =
                        response.getData().getHisseYuzeysel();

                StockPriceData priceData = StockPriceData.builder()
                        .symbol(stock.getSymbol())
                        .lastPrice(hisse.getKapanis())
                        .bidPrice(hisse.getAlis())
                        .askPrice(hisse.getSatis())
                        .dayHigh(hisse.getYuksek())
                        .dayLow(hisse.getDusuk())
                        .dayLimitUp(hisse.getTavan())
                        .dayLimitDown(hisse.getTaban())
                        .changePercentage(hisse.getYuzdedegisim())
                        .volume(hisse.getHacimlot())
                        .turnover(hisse.getHacimtl())
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

        log.info("BigPara: fetched {}/{} stock prices",
                results.size(), stocks.size());
        return results;
    }
}