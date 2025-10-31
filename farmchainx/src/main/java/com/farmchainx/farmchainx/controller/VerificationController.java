package com.farmchainx.farmchainx.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.farmchainx.farmchainx.service.ProductService;

import java.security.Principal;

@RestController
@RequestMapping("/api/verify")
public class VerificationController {

    private final ProductService productService;

    public VerificationController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/{productId}")
    public ResponseEntity<?> verifyProduct(@PathVariable Long productId, Principal principal) {

        System.out.println("🟢 [VERIFY] API called for Product ID: " + productId);

        if (principal == null) {
            System.out.println("🔵 [VERIFY] User not logged in → Returning Public View");
            return ResponseEntity.ok(productService.getPublicView(productId));
        }

        System.out.println("🟡 [VERIFY] Logged in as: " + principal.getName());

        var authentication = (UsernamePasswordAuthenticationToken)
                SecurityContextHolder.getContext().getAuthentication();

        System.out.println("🟣 [VERIFY] Roles: " + authentication.getAuthorities());

        boolean isAuthorized = authentication.getAuthorities().stream()
                .anyMatch(auth -> {
                    String role = auth.getAuthority();
                    System.out.println("   🔍 Checking role: " + role);
                    return role.equals("ROLE_DISTRIBUTER")
                            || role.equals("ROLE_RETAILER")
                            || role.equals("ROLE_ADMIN");
                });

        if (isAuthorized) {
            System.out.println("✅ [VERIFY] Authorized role detected → Returning Authorized View");
            return ResponseEntity.ok(productService.getAuthorizedView(productId, null));
        }

        System.out.println("⚪ [VERIFY] User logged in but not authorized → Returning Public View");
        return ResponseEntity.ok(productService.getPublicView(productId));
    }
}
