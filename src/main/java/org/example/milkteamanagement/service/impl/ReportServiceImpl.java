package org.example.milkteamanagement.service.impl;

import org.example.milkteamanagement.dto.report.ReportGroupBy;
import org.example.milkteamanagement.dto.report.RevenueReportResponse;
import org.example.milkteamanagement.dto.report.TopSellingItemResponse;
import org.example.milkteamanagement.entity.CustomerOrder;
import org.example.milkteamanagement.entity.OrderItem;
import org.example.milkteamanagement.entity.OrderItemTopping;
import org.example.milkteamanagement.entity.ProductRecipe;
import org.example.milkteamanagement.repository.CustomerOrderRepository;
import org.example.milkteamanagement.repository.OrderItemRepository;
import org.example.milkteamanagement.repository.OrderItemToppingRepository;
import org.example.milkteamanagement.repository.ProductRecipeRepository;
import org.example.milkteamanagement.repository.PaymentTransactionRepository;
import org.example.milkteamanagement.service.ReportService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportServiceImpl implements ReportService {

    private final CustomerOrderRepository customerOrderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderItemToppingRepository orderItemToppingRepository;
    private final ProductRecipeRepository productRecipeRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;

    public ReportServiceImpl(CustomerOrderRepository customerOrderRepository,
                             OrderItemRepository orderItemRepository,
                             OrderItemToppingRepository orderItemToppingRepository,
                             ProductRecipeRepository productRecipeRepository,
                             PaymentTransactionRepository paymentTransactionRepository) {
        this.customerOrderRepository = customerOrderRepository;
        this.orderItemRepository = orderItemRepository;
        this.orderItemToppingRepository = orderItemToppingRepository;
        this.productRecipeRepository = productRecipeRepository;
        this.paymentTransactionRepository = paymentTransactionRepository;
    }

    @Override
    public RevenueReportResponse revenue(LocalDateTime from, LocalDateTime to, ReportGroupBy groupBy) {
        // Determine paid orders by checking successful payment transactions
        List<CustomerOrder> allOrders = customerOrderRepository.findByCreatedAtBetween(from, to);
        List<CustomerOrder> paidOrders = allOrders.stream()
                .filter(order -> {
                    List<org.example.milkteamanagement.entity.PaymentTransaction> payments =
                            paymentTransactionRepository.findByOrderAndStatus(order, org.example.milkteamanagement.entity.enums.PaymentStatus.SUCCESS);
                    java.math.BigDecimal sum = java.math.BigDecimal.ZERO;
                    for (var p : payments) sum = sum.add(p.getPaidAmount());
                    return sum.compareTo(order.getTotalAmount()) >= 0;
                })
                .toList();

        BigDecimal revenue = paidOrders.stream()
                .map(CustomerOrder::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal ingredientCost = calcIngredientCost(paidOrders);
        BigDecimal profit = revenue.subtract(ingredientCost);

        List<TopSellingItemResponse> topSelling = orderItemRepository.findTopSellingProductsForOrders(paidOrders).stream()
                .limit(10)
                .map(row -> new TopSellingItemResponse((String) row[0], ((Number) row[1]).longValue()))
                .toList();

        return new RevenueReportResponse(revenue, ingredientCost, profit, topSelling, timeline(paidOrders, groupBy));
    }

    private BigDecimal calcIngredientCost(List<CustomerOrder> orders) {
        BigDecimal totalCost = BigDecimal.ZERO;
        for (CustomerOrder order : orders) {
            List<OrderItem> items = orderItemRepository.findByOrder(order);
            for (OrderItem item : items) {
                totalCost = totalCost.add(itemCost(item.getProduct().getId(), item.getQuantity()));
            }
            List<OrderItemTopping> toppings = orderItemToppingRepository.findByOrderItemIn(items);
            for (OrderItemTopping topping : toppings) {
                totalCost = totalCost.add(itemCost(topping.getToppingProduct().getId(), topping.getQuantity()));
            }
        }
        return totalCost;
    }

    private BigDecimal itemCost(Long productId, Integer quantity) {
        List<ProductRecipe> recipes = productRecipeRepository.findByProductId(productId);
        if (recipes.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal cost = BigDecimal.ZERO;
        for (ProductRecipe recipe : recipes) {
            cost = cost.add(recipe.getIngredient().getCostPerUnit()
                    .multiply(recipe.getQuantityRequired())
                    .multiply(BigDecimal.valueOf(quantity)));
        }
        return cost;
    }

    private List<Map<String, Object>> timeline(List<CustomerOrder> orders, ReportGroupBy groupBy) {
        DateTimeFormatter formatter = switch (groupBy) {
            case YEAR -> DateTimeFormatter.ofPattern("yyyy");
            case MONTH -> DateTimeFormatter.ofPattern("yyyy-MM");
            default -> DateTimeFormatter.ofPattern("yyyy-MM-dd");
        };

        Map<String, BigDecimal> buckets = new HashMap<>();
        for (CustomerOrder order : orders) {
            String key = order.getCreatedAt().format(formatter);
            buckets.put(key, buckets.getOrDefault(key, BigDecimal.ZERO).add(order.getTotalAmount()));
        }

        return buckets.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    Map<String, Object> point = new HashMap<>();
                    point.put("period", entry.getKey());
                    point.put("revenue", entry.getValue());
                    return point;
                })
                .toList();
    }
}

