package com.yusuf.bist_portfolio_management.repository;

import com.yusuf.bist_portfolio_management.entity.AccountLedger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface AccountLedgerRepository extends JpaRepository<AccountLedger, Long> {

    List<AccountLedger> findByAccountIdOrderByCreatedAtDesc(UUID accountId);
}
