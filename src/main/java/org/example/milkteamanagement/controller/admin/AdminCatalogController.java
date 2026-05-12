package org.example.milkteamanagement.controller.admin;

import jakarta.validation.Valid;
import org.example.milkteamanagement.dto.catalog.CategoryRequest;
import org.example.milkteamanagement.dto.catalog.IngredientRequest;
import org.example.milkteamanagement.dto.catalog.ProductRequest;
import org.example.milkteamanagement.dto.catalog.RecipeItemRequest;
import org.example.milkteamanagement.entity.Category;
import org.example.milkteamanagement.entity.Ingredient;
import org.example.milkteamanagement.entity.Product;
import org.example.milkteamanagement.service.CatalogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/catalog")
public class AdminCatalogController {

    private final CatalogService catalogService;

    public AdminCatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @PostMapping("/categories")
    public ResponseEntity<Category> createCategory(@Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(catalogService.createCategory(request));
    }

    @GetMapping("/categories")
    public ResponseEntity<List<Category>> getCategories() {
        return ResponseEntity.ok(catalogService.getCategories());
    }

    @PostMapping("/products")
    public ResponseEntity<Map<String, Object>> createProduct(@Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(toProductPayload(catalogService.createProduct(request)));
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<Map<String, Object>> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(toProductPayload(catalogService.updateProduct(id, request)));
    }

    @GetMapping("/products")
    public ResponseEntity<List<Map<String, Object>>> getProducts() {
        return ResponseEntity.ok(catalogService.getProducts().stream().map(this::toProductPayload).toList());
    }

    @PostMapping("/ingredients")
    public ResponseEntity<Ingredient> createIngredient(@Valid @RequestBody IngredientRequest request) {
        return ResponseEntity.ok(catalogService.createIngredient(request));
    }

    @GetMapping("/ingredients")
    public ResponseEntity<List<Ingredient>> getIngredients() {
        return ResponseEntity.ok(catalogService.getIngredients());
    }

    @PutMapping("/products/{productId}/recipes")
    public ResponseEntity<Void> setRecipe(@PathVariable Long productId, @RequestBody List<RecipeItemRequest> request) {
        catalogService.setRecipe(productId, request);
        return ResponseEntity.noContent().build();
    }

    private Map<String, Object> toProductPayload(Product product) {
        return Map.of(
                "id", product.getId(),
                "name", product.getName(),
                "price", product.getPrice(),
                "status", product.getStatus(),
                "topping", product.isTopping(),
                "category", Map.of(
                        "id", product.getCategory().getId(),
                        "name", product.getCategory().getName()
                )
        );
    }
}

