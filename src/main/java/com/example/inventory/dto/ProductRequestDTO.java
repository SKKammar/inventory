package com.example.inventory.dto;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
@Data @NoArgsConstructor @AllArgsConstructor
public class ProductRequestDTO {
    @NotBlank private String name;
    private String description;
    @NotBlank private String sku;
    private String category;
    @NotNull @DecimalMin("0.0") private BigDecimal price;
    @NotNull @Min(0) private Integer currentStock;
    private Integer minStockLevel;
    private Integer maxStockLevel;
    private String unit;
}
