package org.example.milkteamanagement.controller.staff;

import org.example.milkteamanagement.entity.Product;
import org.example.milkteamanagement.service.CatalogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/staff/products")
public class StaffProductController {

    private final CatalogService catalogService;

    public StaffProductController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping("/available")
    public ResponseEntity<List<Map<String, Object>>> available() {
        return ResponseEntity.ok(catalogService.getAvailableProducts().stream().map(this::toProductPayload).toList());
    }

    private Map<String, Object> toProductPayload(Product product) {
        return Map.of(
                "id", product.getId(),
                "name", product.getName(),
                "price", product.getPrice(),
                "status", product.getStatus(),
                "isTopping", product.isTopping(),
                "category", product.getCategory().getName()
        );
    }
}

