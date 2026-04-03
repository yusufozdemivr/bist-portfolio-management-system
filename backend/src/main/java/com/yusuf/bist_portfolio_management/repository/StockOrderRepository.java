package com.yusuf.bist_portfolio_management.repository;

import com.yusuf.bist_portfolio_management.entity.StockOrder;
import com.yusuf.bist_portfolio_management.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface StockOrderRepository extends JpaRepository<StockOrder, UUID> {

    List<StockOrder> findByAccountIdOrderByCreatedAtDesc(UUID accountId);


    List<StockOrder> findByOrderStatus(OrderStatus orderStatus);


    List<StockOrder> findByAccountIdAndOrderStatus(UUID accountId, OrderStatus orderStatus);

}
