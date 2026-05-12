package org.example.milkteamanagement.controller;

import org.example.milkteamanagement.entity.Category;
import org.example.milkteamanagement.entity.Product;
import org.example.milkteamanagement.repository.CategoryRepository;
import org.example.milkteamanagement.repository.ProductRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminUiController {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public AdminUiController(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @GetMapping({"", "/", "/dashboard"})
    public String dashboard(Model model) {
        long productCount = productRepository.count();
        model.addAttribute("productCount", productCount);
        model.addAttribute("ordersToday", 0);
        model.addAttribute("employeeCount", 0);
        return "admin/dashboard";
    }

    @GetMapping("/products")
    public String products(Model model) {
        List<Product> products = productRepository.findAll();
        model.addAttribute("products", products);
        return "admin/products";
    }

    @GetMapping("/products/new")
    public String newProduct(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryRepository.findAll());
        return "admin/product-form";
    }

    @GetMapping("/products/{id}/edit")
    public String editProduct(@PathVariable Long id, Model model) {
        Product product = productRepository.findById(id).orElse(new Product());
        model.addAttribute("product", product);
        model.addAttribute("categories", categoryRepository.findAll());
        return "admin/product-form";
    }

    @GetMapping("/categories")
    public String categories(Model model) {
        List<Category> categories = categoryRepository.findAll();
        model.addAttribute("categories", categories);
        return "admin/categories"; // create this template later if needed
    }
}

