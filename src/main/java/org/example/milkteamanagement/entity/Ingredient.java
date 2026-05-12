package org.example.milkteamanagement.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.milkteamanagement.entity.enums.IngredientUnit;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ingredients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ingredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private IngredientUnit unit;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal stockQuantity;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal minStockQuantity;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal costPerUnit;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @OneToMany(mappedBy = "ingredient", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ProductRecipe> recipes = new ArrayList<>();
}

