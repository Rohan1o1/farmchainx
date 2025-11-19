package com.farmchainx.farmchainx.repository;

import com.farmchainx.farmchainx.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByFarmerId(Long farmerId);

    Optional<Product> findByPublicUuid(String uuid);

    @Query("SELECT p FROM Product p " +
           "WHERE (:cropName IS NULL OR p.cropName = :cropName) " +
           "AND (:endDate IS NULL OR p.harvestDate <= :endDate)")
    List<Product> filterProducts(
            @Param("cropName") String cropName,
            @Param("endDate") LocalDate endDate
    );
}
