package org.example.milkteamanagement.controller.admin;

import org.example.milkteamanagement.dto.order.OrderResponse;
import org.example.milkteamanagement.entity.CustomerOrder;
import org.example.milkteamanagement.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/orders")
public class AdminOrderController {

    private final OrderService orderService;

    public AdminOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAll() {
        return ResponseEntity.ok(orderService.findAll().stream().map(this::toOrderPayload).toList());
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<OrderResponse> cancel(@PathVariable Long id, Principal principal) {
        return ResponseEntity.ok(orderService.cancel(id, principal.getName()));
    }

    private Map<String, Object> toOrderPayload(CustomerOrder order) {
        return Map.of(
                "id", order.getId(),
                "orderCode", order.getOrderCode(),
                "status", order.getStatus(),
                "subtotal", order.getSubtotal(),
                "discount", order.getDiscountAmount(),
                "total", order.getTotalAmount(),
                "staff", order.getStaff().getUsername(),
                "createdAt", order.getCreatedAt()
        );
    }
}

