package com.yusuf.bist_portfolio_management.repository;

import com.yusuf.bist_portfolio_management.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StockRepository extends JpaRepository<Stock, UUID>{

    Optional<Stock> findBySymbol(String symbol);

    List<Stock> findByIsBist100True();

    List<Stock> findByIsActiveTradingTrue();
}
