package com.yusuf.bist_portfolio_management.repository;

import com.yusuf.bist_portfolio_management.entity.StockPriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StockPriceHistoryRepository extends JpaRepository<StockPriceHistory, Long> {

    List<StockPriceHistory> findByStockIdOrderByPriceDateDesc(UUID stockId);

    Optional<StockPriceHistory> findByStockIdAndPriceDate(UUID stockId, LocalDate priceDate);

}
