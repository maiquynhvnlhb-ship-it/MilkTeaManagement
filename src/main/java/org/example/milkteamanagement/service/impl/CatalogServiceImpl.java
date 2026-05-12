package org.example.milkteamanagement.service.impl;

import org.example.milkteamanagement.dto.catalog.CategoryRequest;
import org.example.milkteamanagement.dto.catalog.IngredientRequest;
import org.example.milkteamanagement.dto.catalog.ProductRequest;
import org.example.milkteamanagement.dto.catalog.RecipeItemRequest;
import org.example.milkteamanagement.entity.Category;
import org.example.milkteamanagement.entity.Ingredient;
import org.example.milkteamanagement.entity.Product;
import org.example.milkteamanagement.entity.ProductRecipe;
import org.example.milkteamanagement.entity.enums.IngredientUnit;
import org.example.milkteamanagement.entity.enums.ProductStatus;
import org.example.milkteamanagement.exception.NotFoundException;
import org.example.milkteamanagement.repository.CategoryRepository;
import org.example.milkteamanagement.repository.IngredientRepository;
import org.example.milkteamanagement.repository.ProductRecipeRepository;
import org.example.milkteamanagement.repository.ProductRepository;
import org.example.milkteamanagement.service.CatalogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CatalogServiceImpl implements CatalogService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final IngredientRepository ingredientRepository;
    private final ProductRecipeRepository productRecipeRepository;

    public CatalogServiceImpl(CategoryRepository categoryRepository,
                              ProductRepository productRepository,
                              IngredientRepository ingredientRepository,
                              ProductRecipeRepository productRecipeRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.ingredientRepository = ingredientRepository;
        this.productRecipeRepository = productRecipeRepository;
    }

    @Override
    public Category createCategory(CategoryRequest request) {
        Category category = new Category();
        category.setName(request.name());
        category.setDescription(request.description());
        category.setActive(request.active());
        return categoryRepository.save(category);
    }

    @Override
    public List<Category> getCategories() {
        return categoryRepository.findAll();
    }

    @Override
    public Product createProduct(ProductRequest request) {
        Product product = new Product();
        applyProduct(request, product);
        return productRepository.save(product);
    }

    @Override
    @Transactional
    public Product updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found: " + id));
        applyProduct(request, product);
        return product;
    }

    @Override
    public List<Product> getProducts() {
        return productRepository.findAll();
    }

    @Override
    public List<Product> getAvailableProducts() {
        return productRepository.findByStatus(ProductStatus.AVAILABLE);
    }

    @Override
    public Ingredient createIngredient(IngredientRequest request) {
        Ingredient ingredient = new Ingredient();
        ingredient.setName(request.name());
        ingredient.setUnit(IngredientUnit.valueOf(request.unit().toUpperCase()));
        ingredient.setStockQuantity(request.stockQuantity());
        ingredient.setMinStockQuantity(request.minStockQuantity());
        ingredient.setCostPerUnit(request.costPerUnit());
        return ingredientRepository.save(ingredient);
    }

    @Override
    public List<Ingredient> getIngredients() {
        return ingredientRepository.findAll();
    }

    @Override
    @Transactional
    public void setRecipe(Long productId, List<RecipeItemRequest> recipeItems) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found: " + productId));
        productRecipeRepository.deleteByProduct(product);
        for (RecipeItemRequest recipeItem : recipeItems) {
            Ingredient ingredient = ingredientRepository.findById(recipeItem.ingredientId())
                    .orElseThrow(() -> new NotFoundException("Ingredient not found: " + recipeItem.ingredientId()));
            ProductRecipe recipe = new ProductRecipe();
            recipe.setProduct(product);
            recipe.setIngredient(ingredient);
            recipe.setQuantityRequired(recipeItem.quantityRequired());
            productRecipeRepository.save(recipe);
        }
    }

    private void applyProduct(ProductRequest request, Product product) {
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new NotFoundException("Category not found: " + request.categoryId()));
        product.setName(request.name());
        product.setCategory(category);
        product.setPrice(request.price());
        product.setTopping(request.topping());
        product.setStatus(request.status() == null || request.status().isBlank()
                ? ProductStatus.AVAILABLE
                : ProductStatus.valueOf(request.status().toUpperCase()));
    }
}

