package com.farmchainx.farmchainx.controller;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.farmchainx.farmchainx.model.Product;
import com.farmchainx.farmchainx.model.SupplyChainLog;
import com.farmchainx.farmchainx.model.User;
import com.farmchainx.farmchainx.repository.FeedbackRepository;
import com.farmchainx.farmchainx.repository.ProductRepository;
import com.farmchainx.farmchainx.repository.SupplyChainLogRepository;
import com.farmchainx.farmchainx.repository.UserRepository;
import com.farmchainx.farmchainx.service.ProductService;
import com.farmchainx.farmchainx.util.HashUtil;
import com.cloudinary.Cloudinary;

@RestController
@RequestMapping("/api")
public class ProductController {

    private final ProductRepository productRepository;
    private final ProductService productService;
    private final UserRepository userRepository;
    private final SupplyChainLogRepository supplyChainLogRepository;
    private final FeedbackRepository feedbackRepository;
    private final Cloudinary cloudinary;

    public ProductController(ProductService productService,
                             UserRepository userRepository,
                             ProductRepository productRepository,
                             SupplyChainLogRepository supplyChainLogRepository,
                             FeedbackRepository feedbackRepository,
                             Cloudinary cloudinary) {
        this.productService = productService;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.supplyChainLogRepository = supplyChainLogRepository;
        this.feedbackRepository = feedbackRepository;
        this.cloudinary = cloudinary;
    }

    @PostMapping("/products/upload")
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<?> uploadProduct(
            @RequestParam String cropName,
            @RequestParam String soilType,
            @RequestParam String pesticides,
            @RequestParam String harvestDate,
            @RequestParam String gpsLocation,
            @RequestParam("image") MultipartFile imageFile,
            Principal principal) throws IOException {

        try {
            if (principal == null) return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
            if (imageFile == null || imageFile.isEmpty()) return ResponseEntity.badRequest().body(Map.of("error", "Image is required"));

            String email = principal.getName();
            User farmer = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Farmer not found"));

            java.util.Map uploadResult = cloudinary.uploader().upload(
                imageFile.getBytes(),
                com.cloudinary.utils.ObjectUtils.asMap("folder", "farmchainx/products")
            );
            String imagePath = uploadResult.get("secure_url").toString();

            LocalDate parsedDate = null;
            if (harvestDate != null && !harvestDate.isBlank()) {
                try {
                    parsedDate = LocalDate.parse(harvestDate, DateTimeFormatter.ISO_DATE);
                } catch (DateTimeParseException ex) {
                    String cleaned = harvestDate.replace(" ", "").replace("−", "-").replace("—", "-").replace("/", "-");
                    parsedDate = LocalDate.parse(cleaned, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
                }
            }

            Product product = new Product();
            product.setCropName(cropName);
            product.setSoilType(soilType);
            product.setPesticides(pesticides);
            product.setHarvestDate(parsedDate);
            product.setGpsLocation(gpsLocation);
            product.setImagePath(imagePath);
            product.setFarmer(farmer);
            product.setQualityGrade(null);
            product.setConfidenceScore(null);

            Product saved = productService.saveProduct(product);
            saved.ensurePublicUuid();
            productRepository.save(saved);

            return ResponseEntity.ok(Map.of(
                    "id", saved.getId(),
                    "message", "Product uploaded successfully",
                    "qualityGrade", saved.getQualityGrade(),
                    "confidenceScore", saved.getConfidenceScore()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Upload failed: " + e.getMessage()));
        }
    }

    @PreAuthorize("hasAnyRole('FARMER', 'ADMIN')")
    @GetMapping("/products/my")
    public ResponseEntity<?> getMyProducts(
            Principal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size,
            @RequestParam(defaultValue = "id,desc") String sort) {

        if (principal == null) return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));

        String email = principal.getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String[] parts = sort.split(",", 2);
        String sortProp = parts[0];
        boolean asc = parts.length > 1 && "asc".equalsIgnoreCase(parts[1]);

        Pageable pageable = PageRequest.of(
                page,
                size,
                asc ? Sort.Direction.ASC : Sort.Direction.DESC,
                sortProp
        );

        // Check if user is admin
        boolean isAdmin = currentUser.getRoles().stream()
                .anyMatch(r -> "ROLE_ADMIN".equalsIgnoreCase(r.getName()));

        Page<?> pageRes;
        if (isAdmin) {
            // Admin sees all products
            pageRes = productRepository.findAll(pageable);
        } else {
            // Farmer sees only their products
            pageRes = productRepository.findByFarmerId(currentUser.getId(), pageable);
        }

        return ResponseEntity.ok(pageRes);
    }

    @PreAuthorize("hasAnyRole('FARMER','DISTRIBUTOR','ADMIN')")
    @PostMapping("/products/{id}/qrcode")
    public ResponseEntity<?> generateProductQrCode(@PathVariable Long id, Principal principal) {
        if (principal == null) return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));

        String email = principal.getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Product product = productService.getProductById(id);
        boolean isOwner = product.getFarmer().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRoles().stream()
                .anyMatch(r -> "ROLE_ADMIN".equalsIgnoreCase(r.getName()));
        boolean isDistributor = currentUser.getRoles().stream()
                .anyMatch(r -> "ROLE_DISTRIBUTOR".equalsIgnoreCase(r.getName()));

        // Allow access if: owner, admin, or distributor (distributors can generate QR for any product they have access to)
        if (!isOwner && !isAdmin && !isDistributor) {
            return ResponseEntity.status(403).body(Map.of("error", "You can only generate QR for your own products"));
        }

        String qrPath = productService.generateProductQr(id);
        return ResponseEntity.ok(Map.of(
            "message", "QR Code generated successfully",
            "qrPath", qrPath,
            "verifyUrl", "https://yourdomain.com/verify/" + product.getPublicUuid()
        ));
    }

    @GetMapping("/products/{id}/qrcode/download")
    public ResponseEntity<byte[]> downloadProductQR(@PathVariable Long id) {
        try {
            byte[] imageBytes = productService.getProductQRImage(id);
            return ResponseEntity.ok()
                    .header("Content-Type", "image/png")
                    .header("Content-Disposition", "attachment; filename=product_" + id + ".png")
                    .body(imageBytes);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(404).body(null);
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN','RETAILER','DISTRIBUTOR')")
    @GetMapping("/products/filter")
    public List<Product> filterProducts(
            @RequestParam(required = false) String cropName,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return productService.filterProducts(cropName, endDate);
    }

    @GetMapping("/products/{id}/public")
    public Map<String, Object> getPublicView(@PathVariable Long id) {
        return productService.getPublicView(id);
    }

    @PreAuthorize("hasAnyRole('FARMER','ADMIN','DISTRIBUTOR','RETAILER')")
    @GetMapping("/products/{id}/details")
    public Map<String, Object> getAuthorizedView(@PathVariable Long id, Principal principal) {
        return productService.getAuthorizedView(id, principal != null ? principal.getName() : null);
    }

    @GetMapping("/products/by-uuid/{uuid}/public")
    public ResponseEntity<?> getPublicByUuid(@PathVariable String uuid) {
        return productRepository.findByPublicUuid(uuid)
                .map(p -> ResponseEntity.ok(productService.getPublicView(p.getId())))
                .orElseGet(() -> ResponseEntity.status(404).body(Map.of("error", "Product not found")));
    }

    @GetMapping("/verify/{uuid}")
    public ResponseEntity<?> verifyByUuid(@PathVariable("uuid") String uuid, Principal principal) {
        try {
            Map<String, Object> data = productService.getPublicViewByUuid(uuid);
            boolean canUpdate = false;
            if (principal != null) {
                User user = userRepository.findByEmail(principal.getName()).orElse(null);
                if (user != null) {
                    canUpdate = user.getRoles().stream().anyMatch(role ->
                        "ROLE_DISTRIBUTOR".equals(role.getName()) || "ROLE_RETAILER".equals(role.getName()));
                }
            }
            data.put("canUpdate", canUpdate);

            boolean canGiveFeedback = false;
            if (principal != null) {
                User user = userRepository.findByEmail(principal.getName()).orElse(null);
                if (user != null) {
                    boolean isConsumer = user.getRoles().stream()
                        .anyMatch(r -> "ROLE_CONSUMER".equals(r.getName()));
                    if (isConsumer) {
                        Long productId = null;
                        if (data.containsKey("productId") && data.get("productId") instanceof Number) {
                            productId = ((Number) data.get("productId")).longValue();
                        } else {
                            productId = productRepository.findByPublicUuid(uuid)
                                .map(p -> p.getId())
                                .orElse(null);
                        }

                        if (productId != null) {
                            boolean already = feedbackRepository.findByProductIdAndConsumerId(productId, user.getId()).isPresent();
                            canGiveFeedback = !already;
                        }
                    }
                }
            }
            data.put("canGiveFeedback", canGiveFeedback);

            return ResponseEntity.ok(data);
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of("error", "Product not found"));
        }
    }

    @PostMapping("/verify/{uuid}/track")
    @PreAuthorize("hasAnyRole('RETAILER','DISTRIBUTOR')")
    public ResponseEntity<?> addTrackingByUuid(
            @PathVariable("uuid") String uuid,
            @RequestBody Map<String, Object> body,
            Principal principal) {

        String location = (String) body.get("location");
        String note = Optional.ofNullable((String) body.get("note")).orElse("").trim();
        Object toUserObj = body.get("toUserId");
        Long toUserId = (toUserObj instanceof Number) ? ((Number) toUserObj).longValue() : null;

        if (location == null || location.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Location is required"));
        }

        User currentUser = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Product product = productRepository.findByPublicUuid(uuid)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Long productId = product.getId();
        SupplyChainLog lastLog = supplyChainLogRepository
                .findTopByProductIdOrderByTimestampDesc(productId)
                .orElse(null);

        if (currentUser.hasRole("ROLE_DISTRIBUTOR")) {

            if (lastLog == null || (lastLog.getToUserId() == null && lastLog.getFromUserId() == null)) {
                SupplyChainLog pickupLog = new SupplyChainLog();
                pickupLog.setProductId(productId);
                pickupLog.setFromUserId(null);
                pickupLog.setToUserId(currentUser.getId());
                pickupLog.setLocation(location);
                pickupLog.setNotes(note.isBlank() ? "Distributor collected from farmer" : note);
                pickupLog.setCreatedBy(currentUser.getEmail());
                pickupLog.setConfirmed(true);
                pickupLog.setTimestamp(LocalDateTime.now());

                String prevHash = lastLog != null ? lastLog.getHash() : "";
                pickupLog.setPrevHash(prevHash);
                pickupLog.setHash(HashUtil.computeHash(pickupLog, prevHash));

                supplyChainLogRepository.save(pickupLog);
                return ResponseEntity.ok(Map.of("message", "You have successfully taken the product from the farmer"));
            }

            if (lastLog.getToUserId() != null && lastLog.getToUserId().equals(currentUser.getId()) && toUserId == null) {
                SupplyChainLog trackingLog = new SupplyChainLog();
                trackingLog.setProductId(productId);
                trackingLog.setFromUserId(currentUser.getId());
                trackingLog.setToUserId(currentUser.getId());
                trackingLog.setLocation(location);
                trackingLog.setNotes(note.isBlank() ? "Tracking update by distributor" : note);
                trackingLog.setCreatedBy(currentUser.getEmail());
                trackingLog.setConfirmed(true);
                trackingLog.setTimestamp(LocalDateTime.now());
                trackingLog.setPrevHash(lastLog.getHash());
                trackingLog.setHash(HashUtil.computeHash(trackingLog, lastLog.getHash()));
                supplyChainLogRepository.save(trackingLog);

                return ResponseEntity.ok(Map.of("message", "Tracking updated"));
            }

            if (toUserId != null && lastLog.getToUserId() != null && lastLog.getToUserId().equals(currentUser.getId()) && lastLog.isConfirmed()) {
                if (!userRepository.existsById(toUserId)) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Selected retailer does not exist"));
                }

                SupplyChainLog handover = new SupplyChainLog();
                handover.setProductId(productId);
                handover.setFromUserId(currentUser.getId());
                handover.setToUserId(toUserId);
                handover.setLocation(location);
                handover.setNotes(note.isBlank() ? "Handed over to retailer – awaiting confirmation" : note);
                handover.setCreatedBy(currentUser.getEmail());
                handover.setConfirmed(false);
                handover.setTimestamp(LocalDateTime.now());
                handover.setPrevHash(lastLog.getHash());
                handover.setHash(HashUtil.computeHash(handover, lastLog.getHash()));

                supplyChainLogRepository.save(handover);

                return ResponseEntity.ok(Map.of("message", "Product successfully handed over! Only the selected retailer will see it in Pending Receipts."));
            }
        }

        if (currentUser.hasRole("ROLE_RETAILER")) {
            if (lastLog == null || !lastLog.getToUserId().equals(currentUser.getId()) || lastLog.isConfirmed()) {
                return ResponseEntity.badRequest().body(Map.of("error", "No product pending for you to confirm"));
            }

            SupplyChainLog confirmLog = new SupplyChainLog();
            confirmLog.setProductId(productId);
            confirmLog.setFromUserId(lastLog.getFromUserId());
            confirmLog.setToUserId(currentUser.getId());
            confirmLog.setLocation(location);
            confirmLog.setNotes(note.isBlank() ? "Retailer confirmed receipt" : note);
            confirmLog.setCreatedBy(currentUser.getEmail());
            confirmLog.setConfirmed(true);
            confirmLog.setConfirmedAt(LocalDateTime.now());
            confirmLog.setConfirmedById(currentUser.getId());
            confirmLog.setTimestamp(LocalDateTime.now());
            confirmLog.setPrevHash(lastLog.getHash());
            confirmLog.setHash(HashUtil.computeHash(confirmLog, lastLog.getHash()));

            supplyChainLogRepository.save(confirmLog);

            return ResponseEntity.ok(Map.of("message", "Receipt confirmed successfully! Chain is now complete."));
        }

        return ResponseEntity.badRequest().body(Map.of("error", "Invalid action"));
    }

    // Endpoint for distributors to see products they currently own/possess
    @PreAuthorize("hasRole('DISTRIBUTOR')")
    @GetMapping("/products/available")
    public ResponseEntity<?> getAvailableProductsForDistributor(
            Principal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size,
            @RequestParam(defaultValue = "id,desc") String sort) {

        if (principal == null) return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));

        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String[] parts = sort.split(",", 2);
        String sortProp = parts[0];
        boolean asc = parts.length > 1 && "asc".equalsIgnoreCase(parts[1]);

        Pageable pageable = PageRequest.of(
                page,
                size,
                asc ? Sort.Direction.ASC : Sort.Direction.DESC,
                sortProp
        );

        // Find products that this distributor currently owns/possesses
        var result = productRepository.findProductsOwnedByDistributor(user.getId(), pageable);
        return ResponseEntity.ok(result);
    }
    
    // Endpoint for distributors to see products available for pickup from farmers
    @PreAuthorize("hasRole('DISTRIBUTOR')")
    @GetMapping("/products/pickup")
    public ResponseEntity<?> getProductsForPickup(
            Principal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size,
            @RequestParam(defaultValue = "id,desc") String sort) {

        if (principal == null) return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));

        String[] parts = sort.split(",", 2);
        String sortProp = parts[0];
        boolean asc = parts.length > 1 && "asc".equalsIgnoreCase(parts[1]);

        Pageable pageable = PageRequest.of(
                page,
                size,
                asc ? Sort.Direction.ASC : Sort.Direction.DESC,
                sortProp
        );

        // Find products that haven't been picked up by any distributor yet
        var result = productRepository.findProductsAvailableForPickup(pageable);
        return ResponseEntity.ok(result);
    }

    // Endpoint for retailers to see available products from distributors
    @PreAuthorize("hasRole('RETAILER')")
    @GetMapping("/products/pending")
    public ResponseEntity<?> getPendingProductsForRetailer(
            Principal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size,
            @RequestParam(defaultValue = "id,desc") String sort) {

        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String[] parts = sort.split(",", 2);
        String sortProp = parts[0];
        boolean asc = parts.length > 1 && "asc".equalsIgnoreCase(parts[1]);

        Pageable pageable = PageRequest.of(
                page,
                size,
                asc ? Sort.Direction.ASC : Sort.Direction.DESC,
                sortProp
        );

        // Find supply chain logs for this retailer and get the associated products
        var supplyChainLogs = supplyChainLogRepository.findProductsPendingForRetailer(user.getId(), pageable);
        
        // Extract product IDs and fetch products
        var productIds = supplyChainLogs.getContent().stream()
                .map(SupplyChainLog::getProductId)
                .distinct()
                .toList();
        
        var products = productRepository.findAllById(productIds);
        
        return ResponseEntity.ok(Map.of(
                "content", products,
                "totalPages", supplyChainLogs.getTotalPages(),
                "totalElements", supplyChainLogs.getTotalElements(),
                "size", supplyChainLogs.getSize(),
                "number", supplyChainLogs.getNumber()
        ));
    }

    // Endpoint for consumers to see available products from retailers
    @PreAuthorize("hasRole('CONSUMER')")
    @GetMapping("/products/retail")
    public ResponseEntity<?> getRetailProductsForConsumer(
            Principal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size,
            @RequestParam(defaultValue = "id,desc") String sort) {

        if (principal == null) return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));

        String[] parts = sort.split(",", 2);
        String sortProp = parts[0];
        boolean asc = parts.length > 1 && "asc".equalsIgnoreCase(parts[1]);

        Pageable pageable = PageRequest.of(
                page,
                size,
                asc ? Sort.Direction.ASC : Sort.Direction.DESC,
                sortProp
        );

        // Find products that have reached retailers and are available to consumers
        var result = productRepository.findProductsAvailableToConsumers(pageable);
        return ResponseEntity.ok(result);
    }
}
