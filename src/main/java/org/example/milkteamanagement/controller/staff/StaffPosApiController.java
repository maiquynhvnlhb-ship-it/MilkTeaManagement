package org.example.milkteamanagement.controller.staff;

import jakarta.validation.Valid;
import org.example.milkteamanagement.dto.order.CheckoutRequest;
import org.example.milkteamanagement.dto.order.CreateOrderRequest;
import org.example.milkteamanagement.dto.order.OrderResponse;
import org.example.milkteamanagement.entity.CustomerOrder;
import org.example.milkteamanagement.entity.PaymentTransaction;
import org.example.milkteamanagement.entity.enums.PaymentStatus;
import org.example.milkteamanagement.service.OrderService;
import org.example.milkteamanagement.repository.CustomerRepository;
import org.example.milkteamanagement.repository.PaymentTransactionRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/staff/pos/api")
public class StaffPosApiController {

    private final OrderService orderService;
    private final CustomerRepository customerRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;

    public StaffPosApiController(OrderService orderService,
                                 CustomerRepository customerRepository,
                                 PaymentTransactionRepository paymentTransactionRepository) {
        this.orderService = orderService;
        this.customerRepository = customerRepository;
        this.paymentTransactionRepository = paymentTransactionRepository;
    }

    @GetMapping("/customers/{phone}")
    public ResponseEntity<?> getCustomer(@PathVariable String phone) {
        return customerRepository.findById(phone)
                .map(c -> ResponseEntity.ok(Map.of(
                        "phone", c.getPhone(),
                        "name", c.getName(),
                        "defaultAddress", c.getDefaultAddress()
                )))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/orders")
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request,
                                                     Principal principal) {
        return ResponseEntity.ok(orderService.createOrder(request, principal.getName()));
    }

    @PostMapping("/orders/{orderId}/checkout")
    public ResponseEntity<OrderResponse> checkout(@PathVariable Long orderId,
                                                  @Valid @RequestBody CheckoutRequest request,
                                                  Principal principal) {
        return ResponseEntity.ok(orderService.checkout(orderId, request, principal.getName()));
    }

    @GetMapping("/orders/{orderId}")
    @Transactional
    public ResponseEntity<?> getOrderDetails(@PathVariable Long orderId) {
        try {
            CustomerOrder order = orderService.findById(orderId);
            List<PaymentTransaction> successPayments = paymentTransactionRepository.findByOrderAndStatus(order, PaymentStatus.SUCCESS);
            java.math.BigDecimal paidTotal = java.math.BigDecimal.ZERO;
            for (PaymentTransaction pt : successPayments) {
                if (pt.getPaidAmount() != null) {
                    paidTotal = paidTotal.add(pt.getPaidAmount());
                }
            }
            boolean paid = order.getTotalAmount() != null && paidTotal.compareTo(order.getTotalAmount()) >= 0;
            boolean canComplete = paid && (order.getStatus() == org.example.milkteamanagement.entity.enums.OrderStatus.READY
                    || order.getStatus() == org.example.milkteamanagement.entity.enums.OrderStatus.PENDING);

            List<Map<String, Object>> items = order.getItems() == null ? List.of() : order.getItems().stream().map(oi -> {
                List<Map<String, Object>> toppings = (oi.getToppings() == null) ? List.of() : oi.getToppings().stream().map(t -> {
                    var prod = t.getToppingProduct();
                    Map<String, Object> toppingMap = new HashMap<>();
                    toppingMap.put("productId", prod != null ? prod.getId() : null);
                    toppingMap.put("name", prod != null ? prod.getName() : null);
                    toppingMap.put("unitPrice", t.getUnitPrice());
                    toppingMap.put("quantity", t.getQuantity());
                    return toppingMap;
                }).toList();

                var prod = oi.getProduct();
                Map<String, Object> itemMap = new HashMap<>();
                itemMap.put("productId", prod != null ? prod.getId() : null);
                itemMap.put("productName", prod != null ? prod.getName() : null);
                itemMap.put("quantity", oi.getQuantity());
                itemMap.put("unitPrice", oi.getUnitPrice());
                itemMap.put("toppings", toppings);
                return itemMap;
            }).toList();

            Map<String, Object> resp = new HashMap<>();
            resp.put("orderId", order.getId());
            resp.put("orderCode", order.getOrderCode());
            resp.put("customerName", order.getCustomer() != null ? order.getCustomer().getName() : null);
            resp.put("customerPhone", order.getCustomer() != null ? order.getCustomer().getPhone() : null);
            resp.put("deliveryAddress", order.getDeliveryAddress());
            resp.put("deliveryType", order.getDeliveryType() != null ? order.getDeliveryType().name() : null);
            resp.put("status", order.getStatus() != null ? order.getStatus().name() : null);
            resp.put("paid", paid);
            resp.put("canComplete", canComplete);
            resp.put("items", items);

            return ResponseEntity.ok(resp);
        } catch (Exception ex) {
            // return structured JSON error rather than causing generic 500 page
            Map<String, Object> err = new HashMap<>();
            err.put("error", "Cannot load order details");
            err.put("message", ex.getMessage());
            return ResponseEntity.status(500).body(err);
        }
    }

    @PostMapping("/orders/{orderId}/send-kitchen")
    public ResponseEntity<OrderResponse> sendToKitchen(@PathVariable Long orderId, Principal principal) {
        return ResponseEntity.ok(orderService.sendToKitchen(orderId, principal.getName()));
    }

    @PostMapping("/orders/{orderId}/pending")
    public ResponseEntity<OrderResponse> markPending(@PathVariable Long orderId, Principal principal) {
        return ResponseEntity.ok(orderService.markPending(orderId, principal.getName()));
    }

    @PostMapping("/orders/{orderId}/ready")
    public ResponseEntity<OrderResponse> markReady(@PathVariable Long orderId, Principal principal) {
        return ResponseEntity.ok(orderService.markReady(orderId, principal.getName()));
    }

    @PostMapping("/orders/{orderId}/complete")
    public ResponseEntity<OrderResponse> complete(@PathVariable Long orderId, Principal principal) {
        return ResponseEntity.ok(orderService.completeOrder(orderId, principal.getName()));
    }

    @PostMapping("/orders/{orderId}/cancel")
    public ResponseEntity<Map<String, Object>> cancel(@PathVariable Long orderId,
                                                      Principal principal) {
        orderService.cancel(orderId, principal.getName());
        return ResponseEntity.ok(Map.of("success", true, "orderId", orderId));
    }
}




