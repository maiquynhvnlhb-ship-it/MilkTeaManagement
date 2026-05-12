package org.example.milkteamanagement.controller.staff;

import org.example.milkteamanagement.entity.CustomerOrder;
import org.example.milkteamanagement.entity.Employee;
import org.example.milkteamanagement.entity.Product;
import org.example.milkteamanagement.entity.UserAccount;
import org.example.milkteamanagement.repository.EmployeeRepository;
import org.example.milkteamanagement.repository.UserAccountRepository;
import org.example.milkteamanagement.service.CatalogService;
import org.example.milkteamanagement.service.OrderService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Comparator;
import java.util.List;

@Controller
@RequestMapping("/staff")
public class StaffUiController {

    private final CatalogService catalogService;
    private final OrderService orderService;
    private final UserAccountRepository userAccountRepository;
    private final EmployeeRepository employeeRepository;

    public StaffUiController(CatalogService catalogService,
                             OrderService orderService,
                             UserAccountRepository userAccountRepository,
                             EmployeeRepository employeeRepository) {
        this.catalogService = catalogService;
        this.orderService = orderService;
        this.userAccountRepository = userAccountRepository;
        this.employeeRepository = employeeRepository;
    }

    @GetMapping({"", "/", "/pos"})
    public String pos(Authentication authentication, Model model) {
        String username = authentication != null ? authentication.getName() : "staff";
        UserAccount user = userAccountRepository.findByUsername(username).orElse(null);
        Employee employee = user != null ? employeeRepository.findByUserAccount(user).orElse(null) : null;

        List<Product> products = catalogService.getAvailableProducts();
        List<CustomerOrder> recentOrders = user != null
                ? orderService.findByStaff(username).stream()
                // only include incomplete orders (not COMPLETED and not CANCELED)
                .filter(o -> o.getStatus() != org.example.milkteamanagement.entity.enums.OrderStatus.COMPLETED && o.getStatus() != org.example.milkteamanagement.entity.enums.OrderStatus.CANCELED)
                .sorted(Comparator.comparing(CustomerOrder::getCreatedAt).reversed())
                .limit(6)
                .toList()
                : List.of();

        model.addAttribute("username", username);
        model.addAttribute("employee", employee);
        model.addAttribute("categories", catalogService.getCategories());
        model.addAttribute("products", products);
        model.addAttribute("recentOrders", recentOrders);
        model.addAttribute("activeMenu", "pos");
        return "staff/pos";
    }

    @GetMapping("/orders")
    public String orders(Authentication authentication, Model model) {
        String username = authentication != null ? authentication.getName() : "staff";
        model.addAttribute("username", username);
        model.addAttribute("orders", orderService.findByStaff(username));
        model.addAttribute("activeMenu", "orders");
        return "staff/orders";
    }

    @GetMapping("/profile")
    public String profile(Authentication authentication, Model model) {
        String username = authentication != null ? authentication.getName() : "staff";
        UserAccount user = userAccountRepository.findByUsername(username).orElse(null);
        Employee employee = user != null ? employeeRepository.findByUserAccount(user).orElse(null) : null;
        model.addAttribute("username", username);
        model.addAttribute("user", user);
        model.addAttribute("employee", employee);
        model.addAttribute("activeMenu", "profile");
        return "staff/profile";
    }
}

