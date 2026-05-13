package org.example.milkteamanagement.controller.staff;

import jakarta.validation.Valid;
import org.example.milkteamanagement.dto.order.CheckoutRequest;
import org.example.milkteamanagement.dto.order.CreateOrderRequest;
import org.example.milkteamanagement.dto.order.OrderResponse;
import org.example.milkteamanagement.entity.CustomerOrder;
import org.example.milkteamanagement.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/staff/orders")
public class StaffOrderController {

    private final OrderService orderService;

    public StaffOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody CreateOrderRequest request, Principal principal) {
        return ResponseEntity.ok(orderService.createOrder(request, principal.getName()));
    }

    @PostMapping("/{orderId}/checkout")
    public ResponseEntity<OrderResponse> checkout(@PathVariable Long orderId,
                                                  @Valid @RequestBody CheckoutRequest request,
                                                  Principal principal) {
        return ResponseEntity.ok(orderService.checkout(orderId, request, principal.getName()));
    }

    @GetMapping("/me")
    public ResponseEntity<List<Map<String, Object>>> myOrders(Principal principal) {
        return ResponseEntity.ok(orderService.findByStaff(principal.getName()).stream().map(this::toOrderPayload).toList());
    }

    @GetMapping("/{orderId}/bill")
    public ResponseEntity<Map<String, Object>> bill(@PathVariable Long orderId) {
        CustomerOrder order = orderService.findById(orderId);
        return ResponseEntity.ok(Map.of(
                "orderCode", order.getOrderCode(),
                "status", order.getStatus(),
                "subtotal", order.getSubtotal(),
                "discount", order.getDiscountAmount(),
                "total", order.getTotalAmount(),
                "thankYou", "Thank you for your purchase!"
        ));
    }

    private Map<String, Object> toOrderPayload(CustomerOrder order) {
        return Map.of(
                "id", order.getId(),
                "orderId", order.getId(),
                "orderCode", order.getOrderCode(),
                "status", order.getStatus(),
                "total", order.getTotalAmount(),
                "createdAt", order.getCreatedAt()
        );
    }
}


