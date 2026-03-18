package com.example.inventory.dto;
import jakarta.validation.constraints.*;
import lombok.*;
@Data @NoArgsConstructor @AllArgsConstructor
public class OrderItemRequestDTO {
    @NotNull private Long productId;
    @NotNull @Min(1) private Integer quantity;
}
