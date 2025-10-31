package com.farmchainx.farmchainx.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // ✅ use Spring’s version
import com.farmchainx.farmchainx.model.Product;
import com.farmchainx.farmchainx.model.User;
import com.farmchainx.farmchainx.repository.ProductRepository;
import com.farmchainx.farmchainx.repository.UserRepository;
import com.farmchainx.farmchainx.util.QrCodeGenerator;
import com.google.zxing.WriterException;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final AiService aiService;
 

    public ProductService(ProductRepository productRepository, UserRepository userRepository, AiService aiService) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.aiService = aiService;
    }

    @Transactional
    public Product saveProduct(Product product) {
    	   	
        System.out.println("✅ [Service] Saving product: " + product.getCropName());
        Product saved = productRepository.save(product);
        System.out.println("✅ [Service] After save, ID = " + saved.getId());
        
        try {
        if(saved.getImagePath()!=null) {
        	Map<String, Object> aiResult = aiService.predictQuality(saved.getImagePath());
        	
        	if(aiResult!=null) {
        		saved.setQualityGrade(String.valueOf(aiResult.get("grade")));
        		
        		saved.setConfidenceScore(Double.parseDouble(aiResult.get("confidence").toString()));
        		
        		productRepository.save(saved);
        		
        		System.out.println("AI Grade: "+saved.getQualityGrade()+ " Confidence Score "+saved.getConfidenceScore());
        	}
        }else {
        	System.out.println("No image path is found for this product Id "+saved.getId());
        }
        }catch(Exception e) {
        	System.out.println("AI Error "+e.getMessage());
        }
        
        return saved;
    }

    
    public List<Product> getProductsByFarmerId(Long farmerId) {
    	User farmer = userRepository.findById(farmerId)
    			.orElseThrow(()->new RuntimeException("Farmer Not Found"));
    	return productRepository.findByFarmerId(farmerId);
        
    }

    public Product getProductById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }
    
    public List<Product> filterProducts(String cropName, LocalDate endDate) {
        return productRepository.filterProducts(cropName, endDate);
    }
    
    public String generateProductQr(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product Not Found"));

        // 🌍 Step 1: Use your machine’s local IP for now (so mobile on same Wi-Fi can access)
        String baseUrl = getServerBaseUrl();
        String qrText = baseUrl + "/api/verify/" + product.getId();

        // 📁 Step 2: Define QR code save location
        String qrPath = "uploads/qrcodes/product_" + productId + ".png";

        try {
            File qrFile = new File(qrPath);
            File parentDir = qrFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                if (!parentDir.mkdirs()) {
                    throw new RuntimeException("Failed to create directory: " + parentDir.getAbsolutePath());
                }
            }

            // 🧩 Step 3: Generate QR
            QrCodeGenerator.generateQR(qrText, qrPath);

            // 🗂️ Step 4: Save path in DB
            product.setQrCodePath(qrPath);
            productRepository.save(product);

            System.out.println("✅ [QR Generated] " + qrText);
            return qrPath;

        } catch (Exception e) {
            throw new RuntimeException("Error generating QR: " + e.getMessage());
        }
    }

   
    private String getServerBaseUrl() {
        try {
            String localIp = java.net.InetAddress.getLocalHost().getHostAddress();
            return "http://" + localIp + ":8081"; // ✅ Works on LAN (Wi-Fi)
        } catch (Exception e) {
            return "http://localhost:8081"; // fallback
        }
    }

    
    public byte[] getProductQRImage(Long productId) {
    	Product product = productRepository.findById(productId)
    			.orElseThrow(()-> new RuntimeException("Product not found"));
    	
    	String qrPath = product.getQrCodePath();
    	
    	if(qrPath == null||qrPath.isEmpty()) {
    		throw new RuntimeException("QR code is not generated yet for this product");
    	}
    	
    	try {
    		Path path = Path.of(qrPath);
    		return Files.readAllBytes(path);
    	}catch(IOException e) {
    		throw new RuntimeException("Unable to read the QR code image :"+e.getMessage());
    	}
    	
    }
    
    public Map<String, Object> getPublicView(Long productId){
    	Product product = productRepository.findById(productId)
    			.orElseThrow(()->new RuntimeException("Product Not found"));
    	
    	Map<String, Object> data = new HashMap<>();
    	
    	data.put("cropName", product.getCropName());
    	data.put("harvestDate", product.getHarvestDate());
          data.put("qualityGrade", product.getQualityGrade());
          data.put("confidence", product.getConfidenceScore());
          data.put("imageUrl", product.getImagePath());
          return data;

    }
    
    public Map<String, Object> getAuthorizedView(Long productId, Object userPrincipal) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Map<String, Object> data = new HashMap<>(getPublicView(productId));
        data.put("soilType", product.getSoilType());
        data.put("pesticides", product.getPesticides());
        data.put("gpsLocation", product.getGpsLocation());

        String requestedBy = "Unknown";

        try {
            if (userPrincipal != null) {
                if (userPrincipal instanceof UserDetails userDetails) {
                    requestedBy = userDetails.getUsername();
                } else if (userPrincipal instanceof String strUser) {
                    requestedBy = strUser;
                }
            }
        } catch (Exception e) {
            System.out.println("⚠️ [AuthorizedView] Unable to resolve user: " + e.getMessage());
        }

        data.put("requestedBy", requestedBy);
        data.put("canUpdateChain", true);
        return data;
    }


    
    
}
