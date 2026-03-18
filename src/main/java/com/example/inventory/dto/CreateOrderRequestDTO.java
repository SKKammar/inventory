package com.example.inventory.dto;
import jakarta.validation.constraints.*;
import lombok.*;
import java.util.List;
@Data @NoArgsConstructor @AllArgsConstructor
public class CreateOrderRequestDTO {
    @NotNull private Long userId;
    @NotEmpty private List<OrderItemRequestDTO> items;
    private String shippingAddress;
}
