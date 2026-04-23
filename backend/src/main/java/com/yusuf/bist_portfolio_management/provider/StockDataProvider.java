package com.yusuf.bist_portfolio_management.provider;

import com.yusuf.bist_portfolio_management.dto.StockPriceData;
import com.yusuf.bist_portfolio_management.entity.Stock;

import java.util.List;

public interface StockDataProvider {

    List<StockPriceData> fetchPrices(List<Stock> stocks);
}