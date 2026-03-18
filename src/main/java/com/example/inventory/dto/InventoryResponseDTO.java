package com.example.inventory.dto;
import com.example.inventory.entity.Inventory.MovementType;
import lombok.*;
import java.time.LocalDateTime;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class InventoryResponseDTO {
    private Long id;
    private Long productId;
    private String productName;
    private MovementType movementType;
    private Integer quantity;
    private Integer previousStock;
    private Integer newStock;
    private String reason;
    private LocalDateTime createdAt;
}
