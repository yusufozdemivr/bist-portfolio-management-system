package com.yusuf.bist_portfolio_management.repository;

import com.yusuf.bist_portfolio_management.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StockRepository extends JpaRepository<Stock, UUID> {

    Optional<Stock> findBySymbol(String symbol);

    List<Stock> findByIsBist100True();

    @Query("SELECT s FROM Stock s LEFT JOIN FETCH s.liveData WHERE s.isBist100 = true")
    List<Stock> findByIsBist100TrueWithLiveData();

    List<Stock> findByIsActiveTradingTrue();
}