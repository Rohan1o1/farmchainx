package com.farmchainx.farmchainx.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.farmchainx.farmchainx.model.Product;
import com.farmchainx.farmchainx.repository.ProductRepository;

@Service
public class ProductService {
	
	private final ProductRepository productRepository;
	
	public ProductService(ProductRepository productRepository) {
		this.productRepository = productRepository;
	}
	
	public Product saveProduct(Product product) {
		return productRepository.save(product);
	}
	
	public List<Product> getProductsByFarmerId(Long farmerId){
		return productRepository.findByFarmer(farmerId);
	}
	
	public Product getProductsById(Long productId) {
		return productRepository.findById(productId)
				.orElseThrow(()-> new RuntimeException("Product not found with ID "+productId));
	}

}
