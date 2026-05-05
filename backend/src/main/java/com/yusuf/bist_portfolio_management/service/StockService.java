package com.yusuf.bist_portfolio_management.service;

import com.yusuf.bist_portfolio_management.dto.StockResponse;
import com.yusuf.bist_portfolio_management.entity.Stock;
import com.yusuf.bist_portfolio_management.entity.StockLiveData;
import com.yusuf.bist_portfolio_management.exception.StockNotFoundException;
import com.yusuf.bist_portfolio_management.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockService {

    private final StockRepository stockRepository;

    public List<StockResponse> getAllStocks() {
        List<Stock> stocks = stockRepository.findByIsBist100TrueWithLiveData();
        return stocks.stream()
                .map(this::mapToResponse)
                .toList();
    }

    public StockResponse getStockBySymbol(String symbol) {
        Stock stock = stockRepository.findBySymbol(symbol.toUpperCase())
                .orElseThrow(() -> new StockNotFoundException(
                        "Stock not found: " + symbol.toUpperCase()));
        return mapToResponse(stock);
    }

    private StockResponse mapToResponse(Stock stock) {
        StockLiveData liveData = stock.getLiveData();

        StockResponse.StockResponseBuilder builder = StockResponse.builder()
                .symbol(stock.getSymbol())
                .companyName(stock.getCompanyName())
                .sector(stock.getSector());

        if (liveData != null) {
            builder.lastPrice(liveData.getLastPrice())
                    .changePercentage(liveData.getChangePercentage())
                    .dayHigh(liveData.getDayHigh())
                    .dayLow(liveData.getDayLow())
                    .bidPrice(liveData.getBidPrice())
                    .askPrice(liveData.getAskPrice())
                    .dayLimitUp(liveData.getDayLimitUp())
                    .dayLimitDown(liveData.getDayLimitDown())
                    .volume(liveData.getVolume())
                    .updatedAt(liveData.getUpdatedAt());
        }

        return builder.build();
    }
}