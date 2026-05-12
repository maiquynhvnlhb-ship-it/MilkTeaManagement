package org.example.milkteamanagement.service;

import org.example.milkteamanagement.dto.catalog.CategoryRequest;
import org.example.milkteamanagement.dto.catalog.IngredientRequest;
import org.example.milkteamanagement.dto.catalog.ProductRequest;
import org.example.milkteamanagement.dto.catalog.RecipeItemRequest;
import org.example.milkteamanagement.entity.Category;
import org.example.milkteamanagement.entity.Ingredient;
import org.example.milkteamanagement.entity.Product;

import java.util.List;

public interface CatalogService {
    Category createCategory(CategoryRequest request);

    List<Category> getCategories();

    Product createProduct(ProductRequest request);

    Product updateProduct(Long id, ProductRequest request);

    List<Product> getProducts();

    List<Product> getAvailableProducts();

    Ingredient createIngredient(IngredientRequest request);

    List<Ingredient> getIngredients();

    void setRecipe(Long productId, List<RecipeItemRequest> recipeItems);
}

