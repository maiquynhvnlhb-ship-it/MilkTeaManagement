package org.example.milkteamanagement.service.impl;

import org.example.milkteamanagement.dto.order.*;
import org.example.milkteamanagement.entity.*;
import org.example.milkteamanagement.entity.enums.*;
import org.example.milkteamanagement.exception.BadRequestException;
import org.example.milkteamanagement.exception.NotFoundException;
import org.example.milkteamanagement.repository.*;
import org.example.milkteamanagement.service.OrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {

    private final UserAccountRepository userAccountRepository;
    private final ProductRepository productRepository;
    private final CustomerOrderRepository customerOrderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderItemToppingRepository orderItemToppingRepository;
    private final VoucherRepository voucherRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final ProductRecipeRepository productRecipeRepository;
    private final IngredientRepository ingredientRepository;
    private final AuditLogRepository auditLogRepository;
    private final CustomerRepository customerRepository;

    public OrderServiceImpl(UserAccountRepository userAccountRepository,
                            ProductRepository productRepository,
                            CustomerOrderRepository customerOrderRepository,
                            OrderItemRepository orderItemRepository,
                            OrderItemToppingRepository orderItemToppingRepository,
                            VoucherRepository voucherRepository,
                            PaymentTransactionRepository paymentTransactionRepository,
                            ProductRecipeRepository productRecipeRepository,
                            IngredientRepository ingredientRepository,
                            AuditLogRepository auditLogRepository,
                            CustomerRepository customerRepository) {
        this.userAccountRepository = userAccountRepository;
        this.productRepository = productRepository;
        this.customerOrderRepository = customerOrderRepository;
        this.orderItemRepository = orderItemRepository;
        this.orderItemToppingRepository = orderItemToppingRepository;
        this.voucherRepository = voucherRepository;
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.productRecipeRepository = productRecipeRepository;
        this.ingredientRepository = ingredientRepository;
        this.auditLogRepository = auditLogRepository;
        this.customerRepository = customerRepository;
    }

    @Override
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request, String staffUsername) {
        UserAccount staff = userAccountRepository.findByUsername(staffUsername)
                .orElseThrow(() -> new NotFoundException("Staff not found: " + staffUsername));

        CustomerOrder order = new CustomerOrder();
        order.setOrderCode("OD" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        order.setStaff(staff);
        // New order created
        order.setStatus(OrderStatus.NEW);
        order.setNote(request.note());

        // delivery type and customer info
        String dType = request.deliveryType();
        org.example.milkteamanagement.entity.enums.DeliveryType deliveryType = org.example.milkteamanagement.entity.enums.DeliveryType.PICKUP;
        if (dType != null && !dType.isBlank()) {
            try {
                deliveryType = org.example.milkteamanagement.entity.enums.DeliveryType.valueOf(dType.toUpperCase());
            } catch (IllegalArgumentException ex) {
                throw new BadRequestException("Invalid deliveryType: " + dType);
            }
        }
        order.setDeliveryType(deliveryType);

        order.setDeliveryAddress(request.deliveryAddress());

        // Handle customer (phone as ID)
        String phone = request.customerPhone();
        org.example.milkteamanagement.entity.Customer customer = null;
        if (phone != null && !phone.isBlank()) {
            customer = customerRepository.findById(phone).map(c -> {
                // if staff provided name or address and they differ, update existing customer
                boolean changed = false;
                if (request.customerName() != null && !request.customerName().isBlank()
                        && !request.customerName().equals(c.getName())) {
                    c.setName(request.customerName());
                    changed = true;
                }
                if (request.deliveryAddress() != null && !request.deliveryAddress().isBlank()
                        && !request.deliveryAddress().equals(c.getDefaultAddress())) {
                    c.setDefaultAddress(request.deliveryAddress());
                    changed = true;
                }
                if (changed) {
                    return customerRepository.save(c);
                }
                return c;
            }).orElseGet(() -> {
                org.example.milkteamanagement.entity.Customer c = new org.example.milkteamanagement.entity.Customer();
                c.setPhone(phone);
                c.setName(request.customerName());
                c.setDefaultAddress(request.deliveryAddress());
                return customerRepository.save(c);
            });
        }

        // If DELIVERY, require phone (customer) and address
        if (deliveryType == org.example.milkteamanagement.entity.enums.DeliveryType.DELIVERY) {
            if (customer == null) {
                throw new BadRequestException("Customer phone is required for DELIVERY orders");
            }
            if (request.deliveryAddress() == null || request.deliveryAddress().isBlank()) {
                throw new BadRequestException("Delivery address is required for DELIVERY orders");
            }
        }

        order.setCustomer(customer);

        // If DELIVERY, require address and customer contact
        if (deliveryType == org.example.milkteamanagement.entity.enums.DeliveryType.DELIVERY) {
            if (request.deliveryAddress() == null || request.deliveryAddress().isBlank()) {
                throw new BadRequestException("Delivery address is required for DELIVERY orders");
            }
            if (request.customerName() == null || request.customerName().isBlank()) {
                throw new BadRequestException("Customer name is required for DELIVERY orders");
            }
            if (request.customerPhone() == null || request.customerPhone().isBlank()) {
                throw new BadRequestException("Customer phone is required for DELIVERY orders");
            }
        }
        customerOrderRepository.save(order);

        // If staff intends to immediately send to kitchen from POS, callers can call sendToKitchen(orderId)

        BigDecimal subtotal = BigDecimal.ZERO;
        for (OrderItemRequest itemRequest : request.items()) {
            Product product = getAvailableProduct(itemRequest.productId());
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(itemRequest.quantity());
            orderItem.setUnitPrice(product.getPrice());
            BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(itemRequest.quantity()));
            orderItem.setLineTotal(itemTotal);
            orderItemRepository.save(orderItem);
            subtotal = subtotal.add(itemTotal);

            if (itemRequest.toppings() != null) {
                for (ToppingRequest toppingRequest : itemRequest.toppings()) {
                    Product topping = getAvailableProduct(toppingRequest.productId());
                    if (!topping.isTopping()) {
                        throw new BadRequestException("Product is not topping: " + topping.getId());
                    }
                    OrderItemTopping orderItemTopping = new OrderItemTopping();
                    orderItemTopping.setOrderItem(orderItem);
                    orderItemTopping.setToppingProduct(topping);
                    orderItemTopping.setQuantity(toppingRequest.quantity());
                    orderItemTopping.setUnitPrice(topping.getPrice());
                    BigDecimal toppingTotal = topping.getPrice().multiply(BigDecimal.valueOf(toppingRequest.quantity()));
                    orderItemTopping.setLineTotal(toppingTotal);
                    orderItemToppingRepository.save(orderItemTopping);
                    subtotal = subtotal.add(toppingTotal);
                }
            }
        }

        BigDecimal discount = calculateDiscount(request.voucherCode(), subtotal);
        BigDecimal total = subtotal.subtract(discount);
        order.setSubtotal(subtotal);
        order.setDiscountAmount(discount);
        order.setTotalAmount(total.max(BigDecimal.ZERO));

        log("CREATE_ORDER", staffUsername, "ORDER", order.getId(), "Create new order");
        return toResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse checkout(Long orderId, CheckoutRequest request, String staffUsername) {
        CustomerOrder order = customerOrderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));
        // allow checkout from multiple statuses (NEW, PENDING, READY)
        if (!(order.getStatus() == OrderStatus.NEW || order.getStatus() == OrderStatus.PENDING || order.getStatus() == OrderStatus.READY || order.getStatus() == OrderStatus.PREPARING)) {
            throw new BadRequestException("Order cannot be checked out in its current status: " + order.getStatus());
        }
        // deduct inventory (only once when actually preparing/checkout)
        deductInventory(order);
        // Payment state is determined by PaymentTransaction(s), not by OrderStatus.

        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setOrder(order);
        transaction.setMethod(PaymentMethod.valueOf(request.paymentMethod().toUpperCase()));
        transaction.setStatus(PaymentStatus.SUCCESS);
        transaction.setPaidAmount(order.getTotalAmount());
        paymentTransactionRepository.save(transaction);
        // Business rule: optionally auto-complete when paid (for pickup immediate). We allow
        // the caller to request completion only if payments cover the total; completeOrder() enforces that.
        customerOrderRepository.save(order);
        if (request != null && Boolean.TRUE.equals(request.autoCompleteOnPaid())) {
            // call completeOrder to enforce payment checks
            return completeOrder(order.getId(), staffUsername);
        }
        log("CHECKOUT_ORDER", staffUsername, "ORDER", order.getId(), "Checkout order");
        return toResponse(order);
    }

    @Transactional
    public OrderResponse sendToKitchen(Long orderId, String staffUsername) {
        CustomerOrder order = customerOrderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));
        if (order.getStatus() != OrderStatus.NEW && order.getStatus() != OrderStatus.PENDING) {
            throw new BadRequestException("Only NEW or PENDING orders can be sent to kitchen");
        }
        order.setStatus(OrderStatus.PREPARING);
        customerOrderRepository.save(order);
        log("SEND_KITCHEN", staffUsername, "ORDER", order.getId(), "Send order to kitchen");
        return toResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse markPending(Long orderId, String staffUsername) {
        CustomerOrder order = customerOrderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));
        if (order.getStatus() != OrderStatus.READY) {
            throw new BadRequestException("Only READY orders can be marked PENDING");
        }
        order.setStatus(OrderStatus.PENDING);
        customerOrderRepository.save(order);
        log("MARK_PENDING", staffUsername, "ORDER", order.getId(), "Order is on delivery");
        return toResponse(order);
    }

    @Transactional
    public OrderResponse markReady(Long orderId, String staffUsername) {
        CustomerOrder order = customerOrderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));
        if (order.getStatus() != OrderStatus.PREPARING) {
            throw new BadRequestException("Only PREPARING orders can be marked READY");
        }
        order.setStatus(OrderStatus.READY);
        customerOrderRepository.save(order);
        log("MARK_READY", staffUsername, "ORDER", order.getId(), "Order is ready");
        return toResponse(order);
    }

    @Transactional
    public OrderResponse completeOrder(Long orderId, String actor) {
        CustomerOrder order = customerOrderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));
        if (order.getStatus() != OrderStatus.READY && order.getStatus() != OrderStatus.PENDING) {
            throw new BadRequestException("Only READY or PENDING orders can be completed");
        }

        // require payment: sum of SUCCESS payments must be >= order total
        List<PaymentTransaction> successPayments = paymentTransactionRepository.findByOrderAndStatus(order, org.example.milkteamanagement.entity.enums.PaymentStatus.SUCCESS);
        java.math.BigDecimal paidTotal = java.math.BigDecimal.ZERO;
        for (PaymentTransaction pt : successPayments) {
            paidTotal = paidTotal.add(pt.getPaidAmount());
        }
        if (paidTotal.compareTo(order.getTotalAmount()) < 0) {
            throw new BadRequestException("Order cannot be completed until payment is received.");
        }

        order.setStatus(OrderStatus.COMPLETED);
        customerOrderRepository.save(order);
        log("COMPLETE_ORDER", actor, "ORDER", order.getId(), "Order completed");
        return toResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse cancel(Long orderId, String actor) {
        CustomerOrder order = customerOrderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));
        // Only NEW orders may be cancelled from POS
        if (order.getStatus() != OrderStatus.NEW) {
            throw new BadRequestException("Only NEW orders can be cancelled.");
        }
        order.setStatus(OrderStatus.CANCELED);
        customerOrderRepository.save(order);
        log("CANCEL_ORDER", actor, "ORDER", order.getId(), "Cancel order");
        return toResponse(order);
    }

    @Override
    public CustomerOrder findById(Long orderId) {
        return customerOrderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));
    }

    @Override
    public List<CustomerOrder> findAll() {
        return customerOrderRepository.findAll();
    }

    @Override
    public List<CustomerOrder> findByStaff(String username) {
        UserAccount staff = userAccountRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Staff not found: " + username));
        return customerOrderRepository.findByStaff(staff);
    }

    private Product getAvailableProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found: " + id));
        if (product.getStatus() != ProductStatus.AVAILABLE) {
            throw new BadRequestException("Product out of stock: " + id);
        }
        return product;
    }

    private BigDecimal calculateDiscount(String voucherCode, BigDecimal subtotal) {
        if (voucherCode == null || voucherCode.isBlank()) {
            return BigDecimal.ZERO;
        }
        LocalDate today = LocalDate.now();
        Voucher voucher = voucherRepository
                .findByCodeIgnoreCaseAndActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        voucherCode,
                        today,
                        today
                )
                .orElseThrow(() -> new BadRequestException("Voucher is invalid or expired"));

        BigDecimal discount;
        if (voucher.getDiscountType() == DiscountType.PERCENT) {
            discount = subtotal.multiply(voucher.getDiscountValue()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } else {
            discount = voucher.getDiscountValue();
        }
        if (discount.compareTo(voucher.getMaxDiscount()) > 0) {
            discount = voucher.getMaxDiscount();
        }
        return discount.min(subtotal);
    }

    private void deductInventory(CustomerOrder order) {
        List<OrderItem> items = orderItemRepository.findByOrder(order);
        for (OrderItem item : items) {
            deductByProduct(item.getProduct(), item.getQuantity());
        }
        List<OrderItemTopping> toppings = orderItemToppingRepository.findByOrderItemIn(items);
        for (OrderItemTopping topping : toppings) {
            deductByProduct(topping.getToppingProduct(), topping.getQuantity());
        }
    }

    private void deductByProduct(Product product, Integer quantity) {
        List<ProductRecipe> recipes = productRecipeRepository.findByProduct(product);
        for (ProductRecipe recipe : recipes) {
            Ingredient ingredient = recipe.getIngredient();
            BigDecimal deduct = recipe.getQuantityRequired().multiply(BigDecimal.valueOf(quantity));
            BigDecimal remain = ingredient.getStockQuantity().subtract(deduct);
            if (remain.compareTo(BigDecimal.ZERO) < 0) {
                throw new BadRequestException("Not enough ingredient: " + ingredient.getName());
            }
            ingredient.setStockQuantity(remain);
            ingredientRepository.save(ingredient);
        }
    }

    private void log(String action, String actorUsername, String targetType, Long targetId, String desc) {
        UserAccount actor = userAccountRepository.findByUsername(actorUsername).orElse(null);
        AuditLog log = new AuditLog();
        log.setAction(action);
        log.setActor(actor);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setDescription(desc);
        auditLogRepository.save(log);
    }

    private OrderResponse toResponse(CustomerOrder order) {
        return new OrderResponse(
                order.getId(),
                order.getOrderCode(),
                order.getStatus(),
                order.getSubtotal(),
                order.getDiscountAmount(),
                order.getTotalAmount(),
                order.getDeliveryType() != null ? order.getDeliveryType().name() : null,
                order.getCustomer() != null ? order.getCustomer().getPhone() : null,
                order.getCustomer() != null ? order.getCustomer().getName() : null,
                order.getDeliveryAddress()
        );
    }
}


