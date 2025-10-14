package com.farmchainx.farmchainx.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.farmchainx.farmchainx.model.Product;
import com.farmchainx.farmchainx.model.User;

public interface ProductRepository extends JpaRepository<Product, Long> {
 
	List<Product> findByFarmer(User farmer);
}
