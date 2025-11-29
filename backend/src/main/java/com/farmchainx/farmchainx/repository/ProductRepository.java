package com.farmchainx.farmchainx.repository;

import com.farmchainx.farmchainx.model.Product;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByFarmerId(Long farmerId);

    Optional<Product> findByPublicUuid(String uuid);

    Page<Product> findByFarmerId(Long farmerId, Pageable pageable);
    
    @Query("SELECT p FROM Product p " +
           "WHERE (:cropName IS NULL OR p.cropName = :cropName) " +
           "AND (:endDate IS NULL OR p.harvestDate <= :endDate)")
    List<Product> filterProducts(
            @Param("cropName") String cropName,
            @Param("endDate") LocalDate endDate
    );

    // Products available for distributors to pick up (not yet in supply chain)
    @Query("SELECT p FROM Product p " +
           "WHERE p.id NOT IN (" +
           "    SELECT DISTINCT sc.productId FROM SupplyChainLog sc " +
           ")")
    Page<Product> findProductsAvailableForPickup(Pageable pageable);
    
    // Products currently owned/possessed by a specific distributor
    @Query("SELECT DISTINCT p FROM Product p " +
           "WHERE p.id IN (" +
           "    SELECT sc1.productId FROM SupplyChainLog sc1 " +
           "    WHERE sc1.toUserId = :distributorId " +
           "    AND sc1.confirmed = true " +
           "    AND NOT EXISTS (" +
           "        SELECT 1 FROM SupplyChainLog sc2 " +
           "        WHERE sc2.productId = sc1.productId " +
           "        AND sc2.fromUserId = :distributorId " +
           "        AND sc2.toUserId != :distributorId " +
           "        AND sc2.confirmed = true " +
           "        AND sc2.timestamp > sc1.timestamp" +
           "    )" +
           ")")
    Page<Product> findProductsOwnedByDistributor(@Param("distributorId") Long distributorId, Pageable pageable);

    // Products available to consumers (have reached retailers)  
    @Query("SELECT DISTINCT p FROM Product p " +
           "WHERE p.id IN (" +
           "    SELECT DISTINCT sc.productId FROM SupplyChainLog sc " +
           "    WHERE sc.confirmed = true" +
           ")")
    Page<Product> findProductsAvailableToConsumers(Pageable pageable);
}
