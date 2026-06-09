package com.yusuf.bist_portfolio_management.repository;

import com.yusuf.bist_portfolio_management.entity.TradeExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TradeExecutionRepository extends JpaRepository<TradeExecution, UUID> {

    List<TradeExecution> findByOrderId(UUID orderId);

    @Query("""
            SELECT e FROM TradeExecution e
            JOIN FETCH e.order o
            WHERE o.account.id = :accountId
              AND o.stock.symbol = :symbol
            ORDER BY e.executedAt DESC
            """)
    List<TradeExecution> findExecutionsByAccountAndSymbol(
            @Param("accountId") UUID accountId,
            @Param("symbol") String symbol);
}