package com.skyline.merchandise.controller;

import com.skyline.auth.model.User;
import com.skyline.auth.repository.UserRepository;
import com.skyline.membership.model.Membership;
import com.skyline.membership.repository.MembershipRepository;
import com.skyline.merchandise.model.*;
import com.skyline.merchandise.repository.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/v1/merchandise")
@RequiredArgsConstructor
public class MerchandiseController {

    private final MerchandiseProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final MerchandiseOrderRepository orderRepository;
    private final UserRepository userRepository;
    private final MembershipRepository membershipRepository;

    @GetMapping("/products")
    public ResponseEntity<List<MerchandiseProduct>> getProducts() {
        return ResponseEntity.ok(productRepository.findAll());
    }

    @PostMapping("/order")
    @Transactional
    public ResponseEntity<?> placeOrder(Authentication authentication, @RequestBody OrderRequest request) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        ProductVariant variant = variantRepository.findById(request.getVariantId())
                .orElseThrow(() -> new RuntimeException("Product variant not found"));

        if (variant.getStockQuantity() < request.getQuantity()) {
            return ResponseEntity.badRequest().body(Map.of("message", "OUT_OF_STOCK: Only " + variant.getStockQuantity() + " left in stock for size " + variant.getSize()));
        }

        boolean isActiveMember = membershipRepository.findByUserId(user.getId())
                .map(m -> m.getStatus() == Membership.MembershipStatus.ACTIVE)
                .orElse(false);

        MerchandiseProduct product = variant.getProduct();
        BigDecimal unitPrice = isActiveMember ? product.getMemberPrice() : product.getPrice();
        BigDecimal totalPrice = unitPrice.multiply(new BigDecimal(request.getQuantity()));

        // Deduct inventory atomically
        variant.setStockQuantity(variant.getStockQuantity() - request.getQuantity());
        variantRepository.save(variant);

        MerchandiseOrder order = MerchandiseOrder.builder()
                .user(user)
                .variant(variant)
                .quantity(request.getQuantity())
                .totalPrice(totalPrice)
                .status(MerchandiseOrder.OrderStatus.PLACED)
                .build();
        orderRepository.save(order);

        return ResponseEntity.ok(Map.of("message", "Hoodie order placed successfully!", "order", order));
    }

    @Data
    public static class OrderRequest {
        private Long variantId;
        private Integer quantity;
    }
}
