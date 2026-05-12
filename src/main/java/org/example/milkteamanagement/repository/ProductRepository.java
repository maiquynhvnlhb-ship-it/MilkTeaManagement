package org.example.milkteamanagement.repository;

import org.example.milkteamanagement.entity.Product;
import org.example.milkteamanagement.entity.enums.ProductStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByStatus(ProductStatus status);

    List<Product> findByToppingTrueAndStatus(ProductStatus status);
}

