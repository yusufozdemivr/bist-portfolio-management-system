package com.yusuf.bist_portfolio_management.scheduler;

import com.yusuf.bist_portfolio_management.dto.StockPriceData;
import com.yusuf.bist_portfolio_management.entity.Stock;
import com.yusuf.bist_portfolio_management.entity.StockLiveData;
import com.yusuf.bist_portfolio_management.provider.StockDataProvider;
import com.yusuf.bist_portfolio_management.repository.StockLiveDataRepository;
import com.yusuf.bist_portfolio_management.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class StockPriceScheduler {

    private final StockDataProvider stockDataProvider;
    private final StockRepository stockRepository;
    private final StockLiveDataRepository stockLiveDataRepository;

    @Scheduled(cron = "0 */5 10-17 * * MON-FRI",
            zone = "Europe/Istanbul")
    @Transactional
    public void fetchStockPrices() {
        log.info("Stock price scheduler started");

        List<Stock> bist100Stocks = stockRepository.findByIsBist100True();

        if (bist100Stocks.isEmpty()) {
            log.warn("No BIST100 stocks found in database");
            return;
        }

        List<StockPriceData> prices = stockDataProvider
                .fetchPrices(bist100Stocks);

        if (prices.isEmpty()) {
            log.warn("No prices fetched from provider");
            return;
        }

        Map<String, Stock> stockMap = bist100Stocks.stream()
                .collect(Collectors.toMap(Stock::getSymbol, stock -> stock));

        int updatedCount = 0;

        for (StockPriceData priceData : prices) {
            Stock stock = stockMap.get(priceData.getSymbol());
            if (stock == null) {
                continue;
            }

            StockLiveData liveData = stockLiveDataRepository
                    .findById(stock.getId())
                    .orElseGet(() -> {
                        StockLiveData newData = new StockLiveData();
                        newData.setStock(stock);
                        return newData;
                    });

            liveData.setLastPrice(priceData.getLastPrice());
            liveData.setBidPrice(priceData.getBidPrice());
            liveData.setAskPrice(priceData.getAskPrice());
            liveData.setDayHigh(priceData.getDayHigh());
            liveData.setDayLow(priceData.getDayLow());
            liveData.setDayLimitUp(priceData.getDayLimitUp());
            liveData.setDayLimitDown(priceData.getDayLimitDown());
            liveData.setChangePercentage(priceData.getChangePercentage());
            liveData.setVolume(priceData.getVolume());
            liveData.setTurnover(priceData.getTurnover());

            stockLiveDataRepository.save(liveData);
            updatedCount++;
        }

        log.info("Stock price scheduler finished: {}/{} prices updated",
                updatedCount, bist100Stocks.size());
    }
}