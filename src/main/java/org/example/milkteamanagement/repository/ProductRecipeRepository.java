package org.example.milkteamanagement.repository;

import org.example.milkteamanagement.entity.Product;
import org.example.milkteamanagement.entity.ProductRecipe;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRecipeRepository extends JpaRepository<ProductRecipe, Long> {
    List<ProductRecipe> findByProduct(Product product);

    List<ProductRecipe> findByProductId(Long productId);

    void deleteByProduct(Product product);
}


