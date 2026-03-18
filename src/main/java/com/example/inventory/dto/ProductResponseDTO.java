package com.example.inventory.dto;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ProductResponseDTO {
    private Long id;
    private String name;
    private String description;
    private String sku;
    private String category;
    private BigDecimal price;
    private Integer currentStock;
    private Integer minStockLevel;
    private Integer maxStockLevel;
    private String unit;
    private Boolean isActive;
    private Boolean isLowStock;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
