package com.yusuf.bist_portfolio_management.repository;

import com.yusuf.bist_portfolio_management.entity.TradeExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TradeExecutionRepository extends JpaRepository<TradeExecution, UUID> {

    List<TradeExecution> findByOrderId(UUID orderId);
}
