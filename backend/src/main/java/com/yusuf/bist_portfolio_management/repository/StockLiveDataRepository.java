package com.yusuf.bist_portfolio_management.repository;

import com.yusuf.bist_portfolio_management.entity.StockLiveData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface StockLiveDataRepository extends JpaRepository<StockLiveData, UUID> {


}
