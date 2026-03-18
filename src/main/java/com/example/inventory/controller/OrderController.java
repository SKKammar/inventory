package com.example.inventory.controller;

import com.example.inventory.dto.CreateOrderRequestDTO;
import com.example.inventory.dto.OrderResponseDTO;
import com.example.inventory.dto.ApiResponseDTO;
import com.example.inventory.entity.Order.OrderStatus;
import com.example.inventory.entity.Order.PaymentStatus;
import com.example.inventory.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Orders", description = "Order management endpoints")
@CrossOrigin(origins = "*", maxAge = 3600)
@SecurityRequirement(name = "Bearer Authentication")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderService orderService;

    /**
     * Create new order
     */
    @PostMapping
    @Operation(summary = "Create order", description = "Create a new order")
    public ResponseEntity<ApiResponseDTO<OrderResponseDTO>> createOrder(@Valid @RequestBody CreateOrderRequestDTO createOrderRequest) {
        logger.info("Creating new order for user: {}", createOrderRequest.getUserId());

        OrderResponseDTO order = orderService.createOrder(createOrderRequest);

        ApiResponseDTO<OrderResponseDTO> response = ApiResponseDTO.<OrderResponseDTO>builder()
                .success(true)
                .message("Order created successfully")
                .data(order)
                .statusCode(201)
                .timestamp(System.currentTimeMillis())
                .build();

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Get all orders (Admin only)
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all orders", description = "Retrieve all orders (Admin only)")
    public ResponseEntity<ApiResponseDTO<List<OrderResponseDTO>>> getAllOrders() {
        logger.info("Fetching all orders");

        List<OrderResponseDTO> orders = orderService.getAllOrders();

        ApiResponseDTO<List<OrderResponseDTO>> response = ApiResponseDTO.<List<OrderResponseDTO>>builder()
                .success(true)
                .message("Orders retrieved successfully")
                .data(orders)
                .statusCode(200)
                .timestamp(System.currentTimeMillis())
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Get order by ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID", description = "Retrieve a specific order by ID")
    public ResponseEntity<ApiResponseDTO<OrderResponseDTO>> getOrderById(@PathVariable Long id) {
        logger.info("Fetching order with ID: {}", id);

        OrderResponseDTO order = orderService.getOrderById(id);

        ApiResponseDTO<OrderResponseDTO> response = ApiResponseDTO.<OrderResponseDTO>builder()
                .success(true)
                .message("Order retrieved successfully")
                .data(order)
                .statusCode(200)
                .timestamp(System.currentTimeMillis())
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Get order by order number
     */
    @GetMapping("/number/{orderNumber}")
    @Operation(summary = "Get order by order number", description = "Retrieve order by order number")
    public ResponseEntity<ApiResponseDTO<OrderResponseDTO>> getOrderByOrderNumber(@PathVariable String orderNumber) {
        logger.info("Fetching order with order number: {}", orderNumber);

        OrderResponseDTO order = orderService.getOrderByOrderNumber(orderNumber);

        ApiResponseDTO<OrderResponseDTO> response = ApiResponseDTO.<OrderResponseDTO>builder()
                .success(true)
                .message("Order retrieved successfully")
                .data(order)
                .statusCode(200)
                .timestamp(System.currentTimeMillis())
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Get user's orders
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "Get user's orders", description = "Retrieve all orders for a specific user")
    public ResponseEntity<ApiResponseDTO<List<OrderResponseDTO>>> getUserOrders(@PathVariable Long userId) {
        logger.info("Fetching orders for user: {}", userId);

        List<OrderResponseDTO> orders = orderService.getUserOrders(userId);

        ApiResponseDTO<List<OrderResponseDTO>> response = ApiResponseDTO.<List<OrderResponseDTO>>builder()
                .success(true)
                .message("Orders retrieved successfully")
                .data(orders)
                .statusCode(200)
                .timestamp(System.currentTimeMillis())
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Get orders by status
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get orders by status", description = "Retrieve all orders with a specific status")
    public ResponseEntity<ApiResponseDTO<List<OrderResponseDTO>>> getOrdersByStatus(@PathVariable OrderStatus status) {
        logger.info("Fetching orders with status: {}", status);

        List<OrderResponseDTO> orders = orderService.getOrdersByStatus(status);

        ApiResponseDTO<List<OrderResponseDTO>> response = ApiResponseDTO.<List<OrderResponseDTO>>builder()
                .success(true)
                .message("Orders retrieved successfully")
                .data(orders)
                .statusCode(200)
                .timestamp(System.currentTimeMillis())
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Update order status
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_STAFF')")
    @Operation(summary = "Update order status", description = "Update the status of an order")
    public ResponseEntity<ApiResponseDTO<OrderResponseDTO>> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam OrderStatus status) {
        logger.info("Updating order status for order: {}, newStatus: {}", id, status);

        OrderResponseDTO order = orderService.updateOrderStatus(id, status);

        ApiResponseDTO<OrderResponseDTO> response = ApiResponseDTO.<OrderResponseDTO>builder()
                .success(true)
                .message("Order status updated successfully")
                .data(order)
                .statusCode(200)
                .timestamp(System.currentTimeMillis())
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Update payment status
     */
    @PutMapping("/{id}/payment-status")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_STAFF')")
    @Operation(summary = "Update payment status", description = "Update the payment status of an order")
    public ResponseEntity<ApiResponseDTO<OrderResponseDTO>> updatePaymentStatus(
            @PathVariable Long id,
            @RequestParam PaymentStatus paymentStatus) {
        logger.info("Updating payment status for order: {}, paymentStatus: {}", id, paymentStatus);

        OrderResponseDTO order = orderService.updatePaymentStatus(id, paymentStatus);

        ApiResponseDTO<OrderResponseDTO> response = ApiResponseDTO.<OrderResponseDTO>builder()
                .success(true)
                .message("Payment status updated successfully")
                .data(order)
                .statusCode(200)
                .timestamp(System.currentTimeMillis())
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Cancel order
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Cancel order", description = "Cancel an existing order")
    public ResponseEntity<ApiResponseDTO<OrderResponseDTO>> cancelOrder(@PathVariable Long id) {
        logger.info("Cancelling order: {}", id);

        OrderResponseDTO order = orderService.cancelOrder(id);

        ApiResponseDTO<OrderResponseDTO> response = ApiResponseDTO.<OrderResponseDTO>builder()
                .success(true)
                .message("Order cancelled successfully")
                .data(order)
                .statusCode(200)
                .timestamp(System.currentTimeMillis())
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Get order statistics
     */
    @GetMapping("/stats/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get order statistics", description = "Retrieve order statistics")
    public ResponseEntity<ApiResponseDTO<Map<String, Object>>> getOrderStatistics() {
        logger.info("Fetching order statistics");

        Map<String, Object> stats = orderService.getOrderStatistics();

        ApiResponseDTO<Map<String, Object>> response = ApiResponseDTO.<Map<String, Object>>builder()
                .success(true)
                .message("Order statistics retrieved successfully")
                .data(stats)
                .statusCode(200)
                .timestamp(System.currentTimeMillis())
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}