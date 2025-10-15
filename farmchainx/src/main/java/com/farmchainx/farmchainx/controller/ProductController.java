package com.farmchainx.farmchainx.controller;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.farmchainx.farmchainx.model.Product;
import com.farmchainx.farmchainx.model.User;
import com.farmchainx.farmchainx.repository.UserRepository;
import com.farmchainx.farmchainx.service.ProductService;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;
    private final UserRepository userRepository;

    public ProductController(ProductService productService, UserRepository userRepository) {
        this.productService = productService;
        this.userRepository = userRepository;
    }

    @PostMapping("/upload")
    public Product uploadProduct(
            @RequestParam String cropName,
            @RequestParam String soilType,
            @RequestParam String pesticides,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate harvestDate,
            @RequestParam String gpsLocation,
            @RequestParam Long farmerId,
            @RequestParam("image") MultipartFile imageFile
    ) throws IOException {

        if (imageFile.isEmpty()) {
            throw new RuntimeException("Image is required");
        }

        // Create upload folder
        String uploadDir = "uploads/";
        File folder = new File(uploadDir);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        // Save image file
        String imagePath = uploadDir + System.currentTimeMillis() + "_" + imageFile.getOriginalFilename();
        imageFile.transferTo(new File(imagePath));

        // Find farmer by ID
        User farmer = userRepository.findById(farmerId)
                .orElseThrow(() -> new RuntimeException("Farmer not found"));

        // ✅ Use builder here
        Product product = Product.builder()
                .cropName(cropName)
                .soilType(soilType)
                .pesticides(pesticides)
                .harvestDate(harvestDate)
                .gpsLocation(gpsLocation)
                .imagePath(imagePath)
                .qualityGrade("Pending") // default or AI-processed later
                .confidenceScore(0.0)    // default
                .build();

        return productService.saveProduct(product);
    }
}
