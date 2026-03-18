package com.example.inventory.service;

import com.example.inventory.dto.InventoryResponseDTO;
import com.example.inventory.dto.StockAdjustmentRequestDTO;
import com.example.inventory.entity.Inventory;
import com.example.inventory.entity.Inventory.MovementType;
import com.example.inventory.entity.Product;
import com.example.inventory.exception.ResourceNotFoundException;
import com.example.inventory.repository.InventoryRepository;
import com.example.inventory.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class InventoryService {

    private static final Logger logger = LoggerFactory.getLogger(InventoryService.class);

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private ProductRepository productRepository;

    public Integer getStockLevel(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
        return product.getCurrentStock();
    }

    public List<InventoryResponseDTO> getStockHistory(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
        return inventoryRepository.findByProductOrderByCreatedAtDesc(product)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<InventoryResponseDTO> getLowStockAlerts() {
        return productRepository.findLowStockProducts().stream()
                .map(p -> InventoryResponseDTO.builder()
                        .productId(p.getId())
                        .productName(p.getName())
                        .newStock(p.getCurrentStock())
                        .build())
                .collect(Collectors.toList());
    }

    public InventoryResponseDTO adjustStock(StockAdjustmentRequestDTO request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", request.getProductId()));

        int previousStock = product.getCurrentStock();
        int newStock;

        switch (request.getMovementType()) {
            case PURCHASE, RETURN -> newStock = previousStock + request.getQuantity();
            case SALE, DAMAGE -> newStock = Math.max(0, previousStock - request.getQuantity());
            case ADJUSTMENT -> newStock = request.getQuantity();
            default -> newStock = previousStock;
        }

        product.setCurrentStock(newStock);
        productRepository.save(product);

        Inventory movement = Inventory.builder()
                .product(product)
                .movementType(request.getMovementType())
                .quantity(request.getQuantity())
                .previousStock(previousStock)
                .newStock(newStock)
                .reason(request.getReason())
                .build();

        Inventory saved = inventoryRepository.save(movement);
        logger.info("Stock adjusted for product {}: {} -> {}", product.getId(), previousStock, newStock);
        return toDTO(saved);
    }

    public boolean isStockAvailable(Long productId, Integer requiredQuantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
        return product.getCurrentStock() >= requiredQuantity;
    }

    public void updateStockForOrder(Long productId, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
        int previousStock = product.getCurrentStock();
        int newStock = previousStock - quantity;
        product.setCurrentStock(newStock);
        productRepository.save(product);

        Inventory movement = Inventory.builder()
                .product(product)
                .movementType(MovementType.SALE)
                .quantity(quantity)
                .previousStock(previousStock)
                .newStock(newStock)
                .reason("Order fulfillment")
                .build();
        inventoryRepository.save(movement);
    }

    public void returnStockForCancelledOrder(Long productId, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
        int previousStock = product.getCurrentStock();
        int newStock = previousStock + quantity;
        product.setCurrentStock(newStock);
        productRepository.save(product);

        Inventory movement = Inventory.builder()
                .product(product)
                .movementType(MovementType.RETURN)
                .quantity(quantity)
                .previousStock(previousStock)
                .newStock(newStock)
                .reason("Order cancelled")
                .build();
        inventoryRepository.save(movement);
    }

    private InventoryResponseDTO toDTO(Inventory inv) {
        return InventoryResponseDTO.builder()
                .id(inv.getId())
                .productId(inv.getProduct().getId())
                .productName(inv.getProduct().getName())
                .movementType(inv.getMovementType())
                .quantity(inv.getQuantity())
                .previousStock(inv.getPreviousStock())
                .newStock(inv.getNewStock())
                .reason(inv.getReason())
                .createdAt(inv.getCreatedAt())
                .build();
    }
}
