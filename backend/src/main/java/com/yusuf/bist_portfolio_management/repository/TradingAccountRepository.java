package com.yusuf.bist_portfolio_management.repository;

import com.yusuf.bist_portfolio_management.entity.TradingAccount;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;


@Repository
public interface TradingAccountRepository extends JpaRepository<TradingAccount, UUID> {

    Optional<TradingAccount> findByUserId(UUID userId);

}
