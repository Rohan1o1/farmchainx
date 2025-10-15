package com.farmchainx.farmchainx.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.farmchainx.farmchainx.model.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByFarmerId(Long farmerId);
}
