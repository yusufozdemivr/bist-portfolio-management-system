package com.yusuf.bist_portfolio_management.repository;

import com.yusuf.bist_portfolio_management.entity.PortfolioPosition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface PortfolioPositionRepository extends JpaRepository<PortfolioPosition, UUID> {

    List<PortfolioPosition> findByAccountId(UUID accountId);

    Optional<PortfolioPosition> findByAccountIdAndStockId(UUID accountId, UUID stockId);

    @Query("""
            SELECT p FROM PortfolioPosition p
            JOIN FETCH p.stock s
            LEFT JOIN FETCH s.liveData
            WHERE p.account.id = :accountId
              AND p.currentQty > 0
            """)
    List<PortfolioPosition> findActivePositionsByAccountId(@Param("accountId") UUID accountId);
}