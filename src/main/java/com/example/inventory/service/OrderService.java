package com.example.inventory.service;

import com.example.inventory.dto.CreateOrderRequestDTO;
import com.example.inventory.dto.OrderItemRequestDTO;
import com.example.inventory.dto.OrderResponseDTO;
import com.example.inventory.dto.OrderItemResponseDTO;
import com.example.inventory.entity.Order;
import com.example.inventory.entity.Order.OrderStatus;
import com.example.inventory.entity.Order.PaymentStatus;
import com.example.inventory.entity.OrderItem;
import com.example.inventory.entity.Product;
import com.example.inventory.entity.User;
import com.example.inventory.exception.InsufficientStockException;
import com.example.inventory.exception.ResourceNotFoundException;
import com.example.inventory.exception.BusinessException;
import com.example.inventory.repository.OrderRepository;
import com.example.inventory.repository.OrderItemRepository;
import com.example.inventory.repository.ProductRepository;
import com.example.inventory.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InventoryService inventoryService;

    /**
     * Create new order
     */
    public OrderResponseDTO createOrder(CreateOrderRequestDTO createOrderRequest) {
        logger.info("Creating new order for user: {}", createOrderRequest.getUserId());

        // Get user
        User user = userRepository.findById(createOrderRequest.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", createOrderRequest.getUserId()));

        // Validate and check stock for all items
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new java.util.ArrayList<>();

        for (OrderItemRequestDTO itemRequest : createOrderRequest.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", "id", itemRequest.getProductId()));

            // Check stock availability
            if (!inventoryService.isStockAvailable(product.getId(), itemRequest.getQuantity())) {
                Integer availableStock = product.getCurrentStock();
                throw new InsufficientStockException(
                        product.getId(),
                        itemRequest.getQuantity(),
                        availableStock
                );
            }

            // Calculate subtotal
            BigDecimal subtotal = product.getPrice().multiply(new BigDecimal(itemRequest.getQuantity()));
            totalAmount = totalAmount.add(subtotal);

            // Create order item
            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .quantity(itemRequest.getQuantity())
                    .unitPrice(product.getPrice())
                    .subtotal(subtotal)
                    .build();

            orderItems.add(orderItem);
        }

        // Create order
        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .user(user)
                .totalAmount(totalAmount)
                .status(OrderStatus.PENDING)
                .paymentStatus(PaymentStatus.UNPAID)
                .shippingAddress(createOrderRequest.getShippingAddress())
                .build();

        Order savedOrder = orderRepository.save(order);

        // Add items to order and update inventory
        for (OrderItem item : orderItems) {
            item.setOrder(savedOrder);
            orderItemRepository.save(item);

            // Update inventory
            inventoryService.updateStockForOrder(item.getProduct().getId(), item.getQuantity());
        }

        logger.info("Order created successfully: {}", savedOrder.getId());

        return convertToResponseDTO(savedOrder);
    }

    /**
     * Get order by ID
     */
    public OrderResponseDTO getOrderById(Long orderId) {
        logger.info("Fetching order with ID: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        return convertToResponseDTO(order);
    }

    /**
     * Get order by order number
     */
    public OrderResponseDTO getOrderByOrderNumber(String orderNumber) {
        logger.info("Fetching order with order number: {}", orderNumber);

        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "orderNumber", orderNumber));

        return convertToResponseDTO(order);
    }

    /**
     * Get all orders (Admin only)
     */
    public List<OrderResponseDTO> getAllOrders() {
        logger.info("Fetching all orders");

        return orderRepository.findAll().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get user's orders
     */
    public List<OrderResponseDTO> getUserOrders(Long userId) {
        logger.info("Fetching orders for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        return orderRepository.findUserOrdersOrderByDateDesc(user).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get orders by status
     */
    public List<OrderResponseDTO> getOrdersByStatus(OrderStatus status) {
        logger.info("Fetching orders with status: {}", status);

        return orderRepository.findByStatus(status).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Update order status
     */
    public OrderResponseDTO updateOrderStatus(Long orderId, OrderStatus newStatus) {
        logger.info("Updating order status for order: {}, newStatus: {}", orderId, newStatus);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        // Validate status transition
        validateStatusTransition(order.getStatus(), newStatus);

        order.setStatus(newStatus);
        Order updatedOrder = orderRepository.save(order);

        logger.info("Order status updated successfully: {}", orderId);

        return convertToResponseDTO(updatedOrder);
    }

    /**
     * Update payment status
     */
    public OrderResponseDTO updatePaymentStatus(Long orderId, PaymentStatus paymentStatus) {
        logger.info("Updating payment status for order: {}, paymentStatus: {}", orderId, paymentStatus);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        order.setPaymentStatus(paymentStatus);
        Order updatedOrder = orderRepository.save(order);

        logger.info("Payment status updated successfully: {}", orderId);

        return convertToResponseDTO(updatedOrder);
    }

    /**
     * Cancel order
     */
    public OrderResponseDTO cancelOrder(Long orderId) {
        logger.info("Cancelling order: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        // Can only cancel PENDING or CONFIRMED orders
        if (!order.getStatus().equals(OrderStatus.PENDING) &&
                !order.getStatus().equals(OrderStatus.CONFIRMED)) {
            throw new BusinessException("INVALID_ORDER_STATUS",
                    "Cannot cancel order in " + order.getStatus() + " status");
        }

        // Return stock for all items
        for (OrderItem item : order.getOrderItems()) {
            inventoryService.returnStockForCancelledOrder(item.getProduct().getId(), item.getQuantity());
        }

        order.setStatus(OrderStatus.CANCELLED);
        Order cancelledOrder = orderRepository.save(order);

        logger.info("Order cancelled successfully: {}", orderId);

        return convertToResponseDTO(cancelledOrder);
    }

    /**
     * Get orders by date range
     */
    public List<OrderResponseDTO> getOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        logger.info("Fetching orders between {} and {}", startDate, endDate);

        return orderRepository.findOrdersByDateRange(startDate, endDate).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get order statistics
     */
    public java.util.Map<String, Object> getOrderStatistics() {
        logger.info("Generating order statistics");

        List<Order> allOrders = orderRepository.findAll();

        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("totalOrders", allOrders.size());
        stats.put("pendingOrders", allOrders.stream().filter(o -> o.getStatus().equals(OrderStatus.PENDING)).count());
        stats.put("confirmedOrders", allOrders.stream().filter(o -> o.getStatus().equals(OrderStatus.CONFIRMED)).count());
        stats.put("shippedOrders", allOrders.stream().filter(o -> o.getStatus().equals(OrderStatus.SHIPPED)).count());
        stats.put("deliveredOrders", allOrders.stream().filter(o -> o.getStatus().equals(OrderStatus.DELIVERED)).count());
        stats.put("cancelledOrders", allOrders.stream().filter(o -> o.getStatus().equals(OrderStatus.CANCELLED)).count());

        BigDecimal totalRevenue = allOrders.stream()
                .filter(o -> o.getStatus().equals(OrderStatus.DELIVERED) && o.getPaymentStatus().equals(PaymentStatus.PAID))
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        stats.put("totalRevenue", totalRevenue);

        return stats;
    }

    /**
     * Generate unique order number
     */
    private String generateOrderNumber() {
        return "ORD-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * Validate order status transition
     */
    private void validateStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        // PENDING -> CONFIRMED, CANCELLED
        // CONFIRMED -> SHIPPED, CANCELLED
        // SHIPPED -> DELIVERED
        // DELIVERED -> (no change)
        // CANCELLED -> (no change)

        if (currentStatus.equals(OrderStatus.CANCELLED) || currentStatus.equals(OrderStatus.DELIVERED)) {
            throw new BusinessException("INVALID_STATUS_TRANSITION",
                    "Cannot change status from " + currentStatus);
        }

        if (currentStatus.equals(OrderStatus.PENDING) &&
                !newStatus.equals(OrderStatus.CONFIRMED) &&
                !newStatus.equals(OrderStatus.CANCELLED)) {
            throw new BusinessException("INVALID_STATUS_TRANSITION",
                    "Invalid status transition from " + currentStatus + " to " + newStatus);
        }

        if (currentStatus.equals(OrderStatus.CONFIRMED) &&
                !newStatus.equals(OrderStatus.SHIPPED) &&
                !newStatus.equals(OrderStatus.CANCELLED)) {
            throw new BusinessException("INVALID_STATUS_TRANSITION",
                    "Invalid status transition from " + currentStatus + " to " + newStatus);
        }

        if (currentStatus.equals(OrderStatus.SHIPPED) &&
                !newStatus.equals(OrderStatus.DELIVERED)) {
            throw new BusinessException("INVALID_STATUS_TRANSITION",
                    "Invalid status transition from " + currentStatus + " to " + newStatus);
        }
    }

    /**
     * Convert Order entity to OrderResponseDTO
     */
    private OrderResponseDTO convertToResponseDTO(Order order) {
        return OrderResponseDTO.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .user(convertUserToDTO(order.getUser()))
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .paymentStatus(order.getPaymentStatus())
                .shippingAddress(order.getShippingAddress())
                .orderDate(order.getOrderDate())
                .createdAt(order.getCreatedAt())
                .items(order.getOrderItems().stream()
                        .map(this::convertOrderItemToDTO)
                        .collect(Collectors.toList()))
                .build();
    }

    /**
     * Convert OrderItem entity to OrderItemResponseDTO
     */
    private OrderItemResponseDTO convertOrderItemToDTO(OrderItem orderItem) {
        return OrderItemResponseDTO.builder()
                .id(orderItem.getId())
                .productId(orderItem.getProduct().getId())
                .productName(orderItem.getProduct().getName())
                .productSku(orderItem.getProduct().getSku())
                .quantity(orderItem.getQuantity())
                .unitPrice(orderItem.getUnitPrice())
                .subtotal(orderItem.getSubtotal())
                .build();
    }

    /**
     * Convert User to UserResponseDTO
     */
    private com.inventory.dto.UserResponseDTO convertUserToDTO(User user) {
        return com.inventory.dto.UserResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .address(user.getAddress())
                .isActive(user.getIsActive())
                .roles(user.getRoles().stream()
                        .map(role -> role.getName())
                        .collect(Collectors.toSet()))
                .build();
    }
}