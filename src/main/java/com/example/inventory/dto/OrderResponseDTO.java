package com.example.inventory.dto;
import com.example.inventory.entity.Order.OrderStatus;
import com.example.inventory.entity.Order.PaymentStatus;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class OrderResponseDTO {
    private Long id;
    private String orderNumber;
    private UserResponseDTO user;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private PaymentStatus paymentStatus;
    private String shippingAddress;
    private LocalDateTime orderDate;
    private LocalDateTime createdAt;
    private List<OrderItemResponseDTO> items;
}
