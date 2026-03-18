package com.example.inventory.controller;

import com.example.inventory.dto.InventoryResponseDTO;
import com.example.inventory.dto.StockAdjustmentRequestDTO;
import com.example.inventory.dto.ApiResponseDTO;
import com.example.inventory.service.InventoryService;
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

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@Tag(name = "Inventory", description = "Inventory management endpoints")
@CrossOrigin(origins = "*", maxAge = 3600)
@SecurityRequirement(name = "Bearer Authentication")
public class InventoryController {

    private static final Logger logger = LoggerFactory.getLogger(InventoryController.class);

    @Autowired
    private InventoryService inventoryService;

    /**
     * Get stock level for a product
     */
    @GetMapping("/stock/{productId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_STAFF')")
    @Operation(summary = "Get stock level", description = "Get current stock level for a product")
    public ResponseEntity<ApiResponseDTO<Integer>> getStockLevel(@PathVariable Long productId) {
        logger.info("Fetching stock level for product: {}", productId);

        Integer stockLevel = inventoryService.getStockLevel(productId);

        ApiResponseDTO<Integer> response = ApiResponseDTO.<Integer>builder()
                .success(true)
                .message("Stock level retrieved successfully")
                .data(stockLevel)
                .statusCode(200)
                .timestamp(System.currentTimeMillis())
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Get stock history for a product
     */
    @GetMapping("/history/{productId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_STAFF')")
    @Operation(summary = "Get stock history", description = "Retrieve stock movement history for a product")
    public ResponseEntity<ApiResponseDTO<List<InventoryResponseDTO>>> getStockHistory(@PathVariable Long productId) {
        logger.info("Fetching stock history for product: {}", productId);

        List<InventoryResponseDTO> history = inventoryService.getStockHistory(productId);

        ApiResponseDTO<List<InventoryResponseDTO>> response = ApiResponseDTO.<List<InventoryResponseDTO>>builder()
                .success(true)
                .message("Stock history retrieved successfully")
                .data(history)
                .statusCode(200)
                .timestamp(System.currentTimeMillis())
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Get low stock alerts
     */
    @GetMapping("/low-stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_STAFF')")
    @Operation(summary = "Get low stock alerts", description = "Retrieve all products with low stock levels")
    public ResponseEntity<ApiResponseDTO<List<InventoryResponseDTO>>> getLowStockAlerts() {
        logger.info("Fetching low stock alerts");

        List<InventoryResponseDTO> lowStockProducts = inventoryService.getLowStockAlerts();

        ApiResponseDTO<List<InventoryResponseDTO>> response = ApiResponseDTO.<List<InventoryResponseDTO>>builder()
                .success(true)
                .message("Low stock alerts retrieved successfully")
                .data(lowStockProducts)
                .statusCode(200)
                .timestamp(System.currentTimeMillis())
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Adjust stock manually
     */
    @PostMapping("/adjustment")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Adjust stock", description = "Manually adjust product stock")
    public ResponseEntity<ApiResponseDTO<InventoryResponseDTO>> adjustStock(
            @Valid @RequestBody StockAdjustmentRequestDTO adjustmentRequest) {
        logger.info("Adjusting stock for product: {}", adjustmentRequest.getProductId());

        InventoryResponseDTO inventory = inventoryService.adjustStock(adjustmentRequest);

        ApiResponseDTO<InventoryResponseDTO> response = ApiResponseDTO.<InventoryResponseDTO>builder()
                .success(true)
                .message("Stock adjusted successfully")
                .data(inventory)
                .statusCode(200)
                .timestamp(System.currentTimeMillis())
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}