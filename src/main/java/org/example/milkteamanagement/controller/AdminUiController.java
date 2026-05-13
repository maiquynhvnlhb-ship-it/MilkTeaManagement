package org.example.milkteamanagement.controller;

import org.example.milkteamanagement.entity.Category;
import org.example.milkteamanagement.entity.Employee;
import org.example.milkteamanagement.entity.Product;
import org.example.milkteamanagement.entity.UserAccount;
import org.example.milkteamanagement.entity.enums.OrderStatus;
import org.example.milkteamanagement.repository.CategoryRepository;
import org.example.milkteamanagement.repository.CustomerOrderRepository;
import org.example.milkteamanagement.repository.EmployeeRepository;
import org.example.milkteamanagement.repository.OrderItemRepository;
import org.example.milkteamanagement.repository.ProductRepository;
import org.example.milkteamanagement.repository.UserAccountRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Controller
@RequestMapping("/admin")
public class AdminUiController {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final EmployeeRepository employeeRepository;
    private final UserAccountRepository userAccountRepository;
    private final CustomerOrderRepository customerOrderRepository;
    private final OrderItemRepository orderItemRepository;

    public AdminUiController(ProductRepository productRepository, 
                             CategoryRepository categoryRepository,
                             EmployeeRepository employeeRepository,
                             UserAccountRepository userAccountRepository,
                             CustomerOrderRepository customerOrderRepository,
                             OrderItemRepository orderItemRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.employeeRepository = employeeRepository;
        this.userAccountRepository = userAccountRepository;
        this.customerOrderRepository = customerOrderRepository;
        this.orderItemRepository = orderItemRepository;
    }

    // --- DASHBOARD ---
    @GetMapping({"", "/", "/dashboard"})
    public String dashboard(Model model) {
        long productCount = productRepository.count();
        long employeeCount = employeeRepository.count();
        
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().plusDays(1).atStartOfDay();
        
        // Count orders today
        List<org.example.milkteamanagement.entity.CustomerOrder> ordersTodayList = 
            customerOrderRepository.findByCreatedAtBetween(startOfDay, endOfDay);
        int ordersToday = ordersTodayList.size();
        
        // Sum revenue today (COMPLETED orders only)
        java.math.BigDecimal revenueToday = customerOrderRepository.sumTotalByStatusAndRange(OrderStatus.COMPLETED, startOfDay, endOfDay);
        if (revenueToday == null) {
            revenueToday = java.math.BigDecimal.ZERO;
        }

        model.addAttribute("productCount", productCount);
        model.addAttribute("ordersToday", ordersToday);
        model.addAttribute("employeeCount", employeeCount);
        model.addAttribute("revenueToday", revenueToday);
        model.addAttribute("activeMenu", "dashboard");
        return "admin/dashboard";
    }

    // --- STATISTICS ---
    @GetMapping("/statistics")
    public String statistics(Model model) {
        LocalDate today = LocalDate.now();
        LocalDateTime firstDayOfMonth = today.withDayOfMonth(1).atStartOfDay();
        LocalDateTime nextMonthFirstDay = today.plusMonths(1).withDayOfMonth(1).atStartOfDay();
        
        // Month to date metrics
        List<org.example.milkteamanagement.entity.CustomerOrder> monthOrdersList = 
            customerOrderRepository.findByCreatedAtBetween(firstDayOfMonth, nextMonthFirstDay);
        int monthOrders = monthOrdersList.size();
        
        java.math.BigDecimal monthRevenue = customerOrderRepository.sumTotalByStatusAndRange(OrderStatus.COMPLETED, firstDayOfMonth, nextMonthFirstDay);
        if (monthRevenue == null) monthRevenue = java.math.BigDecimal.ZERO;

        // 7 days chart data
        List<String> chartLabels = new ArrayList<>();
        List<java.math.BigDecimal> chartData = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
        
        for (int i = 6; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            chartLabels.add(d.format(formatter));
            
            java.math.BigDecimal dailyRev = customerOrderRepository.sumTotalByStatusAndRange(OrderStatus.COMPLETED, d.atStartOfDay(), d.plusDays(1).atStartOfDay());
            chartData.add(dailyRev != null ? dailyRev : java.math.BigDecimal.ZERO);
        }

        // Top selling products (Last 30 days)
        LocalDateTime thirtyDaysAgo = today.minusDays(30).atStartOfDay();
        List<org.example.milkteamanagement.entity.CustomerOrder> last30DaysOrders = 
            customerOrderRepository.findByCreatedAtBetween(thirtyDaysAgo, today.plusDays(1).atStartOfDay());
            
        List<Object[]> topProductsRaw = new ArrayList<>();
        if (!last30DaysOrders.isEmpty()) {
            topProductsRaw = orderItemRepository.findTopSellingProductsForOrders(last30DaysOrders);
        }
        
        // Convert to map
        List<Map<String, Object>> topProducts = new ArrayList<>();
        for (Object[] row : topProductsRaw) {
            Map<String, Object> map = new HashMap<>();
            map.put("name", row[0]);
            map.put("quantity", row[1]);
            topProducts.add(map);
        }

        model.addAttribute("monthOrders", monthOrders);
        model.addAttribute("monthRevenue", monthRevenue);
        model.addAttribute("chartLabels", chartLabels);
        model.addAttribute("chartData", chartData);
        model.addAttribute("topProducts", topProducts);
        
        model.addAttribute("activeMenu", "statistics");
        return "admin/statistics";
    }

    // --- PRODUCTS ---
    @GetMapping("/products")
    public String products(Model model) {
        model.addAttribute("products", productRepository.findAll());
        model.addAttribute("activeMenu", "products");
        return "admin/products";
    }

    @GetMapping("/products/new")
    public String newProduct(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("activeMenu", "products");
        return "admin/product-form";
    }

    @GetMapping("/products/{id}/edit")
    public String editProduct(@PathVariable Long id, Model model) {
        Product product = productRepository.findById(id).orElse(new Product());
        model.addAttribute("product", product);
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("activeMenu", "products");
        return "admin/product-form";
    }

    @PostMapping("/products/save")
    public String saveProduct(@ModelAttribute Product product, @RequestParam Long categoryId) {
        Category category = categoryRepository.findById(categoryId).orElseThrow();
        
        if (product.getId() != null) {
            Product existing = productRepository.findById(product.getId()).orElseThrow();
            existing.setName(product.getName());
            existing.setPrice(product.getPrice());
            existing.setCategory(category);
            existing.setStatus(product.getStatus());
            existing.setTopping(product.isTopping());
            productRepository.save(existing);
        } else {
            product.setCategory(category);
            if (product.getStatus() == null) {
                product.setStatus(org.example.milkteamanagement.entity.enums.ProductStatus.AVAILABLE);
            }
            productRepository.save(product);
        }
        return "redirect:/admin/products";
    }

    @GetMapping("/products/{id}/delete")
    public String deleteProduct(@PathVariable Long id) {
        productRepository.deleteById(id);
        return "redirect:/admin/products";
    }

    // --- CATEGORIES ---
    @GetMapping("/categories")
    public String categories(Model model) {
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("activeMenu", "categories");
        return "admin/categories";
    }
    
    @GetMapping("/categories/new")
    public String newCategory(Model model) {
        model.addAttribute("category", new Category());
        model.addAttribute("activeMenu", "categories");
        return "admin/category-form";
    }

    @GetMapping("/categories/{id}/edit")
    public String editCategory(@PathVariable Long id, Model model) {
        Category category = categoryRepository.findById(id).orElse(new Category());
        model.addAttribute("category", category);
        model.addAttribute("activeMenu", "categories");
        return "admin/category-form";
    }

    @PostMapping("/categories/save")
    public String saveCategory(@ModelAttribute Category category) {
        if (category.getId() != null) {
            Category existing = categoryRepository.findById(category.getId()).orElseThrow();
            existing.setName(category.getName());
            existing.setDescription(category.getDescription());
            existing.setActive(category.isActive());
            categoryRepository.save(existing);
        } else {
            categoryRepository.save(category);
        }
        return "redirect:/admin/categories";
    }

    @GetMapping("/categories/{id}/delete")
    public String deleteCategory(@PathVariable Long id) {
        categoryRepository.deleteById(id);
        return "redirect:/admin/categories";
    }

    // --- EMPLOYEES ---
    @GetMapping("/employees")
    public String employees(Model model) {
        model.addAttribute("employees", employeeRepository.findAll());
        model.addAttribute("activeMenu", "employees");
        return "admin/employees";
    }
    
    @GetMapping("/employees/new")
    public String newEmployee(Model model) {
        model.addAttribute("employee", new Employee());
        model.addAttribute("activeMenu", "employees");
        return "admin/employee-form";
    }

    @GetMapping("/employees/{id}/edit")
    public String editEmployee(@PathVariable Long id, Model model) {
        Employee employee = employeeRepository.findById(id).orElse(new Employee());
        model.addAttribute("employee", employee);
        model.addAttribute("userAccount", employee.getUserAccount());
        model.addAttribute("activeMenu", "employees");
        return "admin/employee-form";
    }

    @PostMapping("/employees/save")
    public String saveEmployee(@ModelAttribute Employee employee, 
                               @RequestParam(required = false) String username,
                               @RequestParam(required = false) String rawPassword,
                               @RequestParam(required = false) String role,
                               @RequestParam(required = false, defaultValue = "false") boolean enabled) {
        
        if (employee.getId() != null) {
            // Edit existing
            Employee existing = employeeRepository.findById(employee.getId()).orElseThrow();
            existing.setFullName(employee.getFullName());
            existing.setPhone(employee.getPhone());
            existing.setStatus(employee.getStatus());
            
            if (existing.getUserAccount() != null) {
                UserAccount user = existing.getUserAccount();
                if (role != null && !role.isEmpty()) {
                    user.setRole(org.example.milkteamanagement.entity.enums.RoleName.valueOf(role));
                }
                user.setEnabled(enabled);
                // In a real app we'd encode rawPassword using PasswordEncoder if not empty
                if (rawPassword != null && !rawPassword.trim().isEmpty()) {
                    user.setPassword("{noop}" + rawPassword.trim());
                }
                userAccountRepository.save(user);
            }
            employeeRepository.save(existing);
        } else {
            // New employee - needs a new UserAccount
            UserAccount newUser = new UserAccount();
            newUser.setUsername(username);
            newUser.setPassword("{noop}" + (rawPassword != null && !rawPassword.trim().isEmpty() ? rawPassword.trim() : "123")); // Default password
            newUser.setRole(role != null && !role.isEmpty() ? org.example.milkteamanagement.entity.enums.RoleName.valueOf(role) : org.example.milkteamanagement.entity.enums.RoleName.STAFF);
            newUser.setEnabled(enabled);
            newUser = userAccountRepository.save(newUser);
            
            employee.setUserAccount(newUser);
            if (employee.getStatus() == null) {
                employee.setStatus(org.example.milkteamanagement.entity.enums.EmployeeStatus.WORKING);
            }
            employeeRepository.save(employee);
        }
        return "redirect:/admin/employees";
    }
}
