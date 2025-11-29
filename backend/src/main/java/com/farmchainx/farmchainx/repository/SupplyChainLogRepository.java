package com.farmchainx.farmchainx.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;

import com.farmchainx.farmchainx.model.SupplyChainLog;

import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

public interface SupplyChainLogRepository extends JpaRepository<SupplyChainLog, Long> {

    List<SupplyChainLog> findByProductIdOrderByTimestampAsc(Long productId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<SupplyChainLog> findTopByProductIdOrderByTimestampDesc(Long productId);

    @Query("SELECT log FROM SupplyChainLog log " +
           "WHERE log.toUserId = :retailerId " +
           "AND log.confirmed = false " +
           "AND log.rejected = false")
    Page<SupplyChainLog> findPendingForRetailer(@Param("retailerId") Long retailerId, Pageable pageable);

    // Get products pending for a specific retailer
    @Query("SELECT sc FROM SupplyChainLog sc " +
           "WHERE sc.toUserId = :retailerId " +
           "AND sc.confirmed = false " +
           "AND sc.rejected = false")
    Page<SupplyChainLog> findProductsPendingForRetailer(@Param("retailerId") Long retailerId, Pageable pageable);
}
