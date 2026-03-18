package com.example.inventory.dto;
import com.example.inventory.entity.Inventory.MovementType;
import jakarta.validation.constraints.*;
import lombok.*;
@Data @NoArgsConstructor @AllArgsConstructor
public class StockAdjustmentRequestDTO {
    @NotNull private Long productId;
    @NotNull private MovementType movementType;
    @NotNull @Min(1) private Integer quantity;
    private String reason;
}
