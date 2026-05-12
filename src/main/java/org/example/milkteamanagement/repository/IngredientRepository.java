package org.example.milkteamanagement.repository;

import org.example.milkteamanagement.entity.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IngredientRepository extends JpaRepository<Ingredient, Long> {
}

