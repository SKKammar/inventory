package com.example.inventory.service;

import com.example.inventory.dto.InventoryResponseDTO;
import com.example.inventory.dto.StockAdjustmentRequestDTO;
import com.example.inventory.entity.Inventory;
import com.example.inventory.entity.Inventory.MovementType;
import com.example.inventory.entity.Product;
import com.example.inventory.entity.User;
import com.example.inventory.exception.ResourceNotFoundException;
import com.example.inventory.repository.InventoryRepository;
import com.example.inventory.repository.ProductRepository;
import com.example.inventory.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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

    @Autowired
    private UserRepository userRepository;

    /**
     * Record inventory movement (internal use)
     */
    public void recordMovement(Long productId, Integer quantity, MovementType movementType, String reason) {
        logger.info("Recording inventory movement for product: {}, type: {}, quantity: {}", productId, movementType, quantity);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        Integer previousStock = product.getCurrentStock();
        Integer newStock = calculateNewStock(previousStock, quantity, movementType);

        // Update product stock
        product.setCurrentStock(newStock);
        productRepository.save(product);

        // Get current user for audit
        User createdBy = getCurrentUser();

        // Create inventory movement record
        Inventory inventory = Inventory.builder()
                .product(product)
                .movementType(movementType)
                .quantity(quantity)
                .previousStock(previousStock)
                .newStock(newStock)
                .reason(reason)
                .createdBy(createdBy)
                .build();

        inventoryRepository.save(inventory);
        logger.info("Inventory movement recorded: product={}, previousStock={}, newStock={}", productId, previousStock, newStock);
    }

    /**
     * Update stock for order (SALE)
     */
    public void updateStockForOrder(Long productId, Integer quantity) {
        logger.info("Updating stock for order: product={}, quantity={}", productId, quantity);
        recordMovement(productId, quantity, MovementType.SALE, "Order placed");
    }

    /**
     * Return stock for cancelled order (RETURN)
     */
    public void returnStockForCancelledOrder(Long productId, Integer quantity) {
        logger.info("Returning stock for cancelled order: product={}, quantity={}", productId, quantity);
        recordMovement(productId, quantity, MovementType.RETURN, "Order cancelled");
    }

    /**
     * Manual stock adjustment
     */
    public InventoryResponseDTO adjustStock(StockAdjustmentRequestDTO adjustmentRequest) {
        logger.info("Adjusting stock: product={}, type={}, quantity={}",
                adjustmentRequest.getProductId(), adjustmentRequest.getMovementType(), adjustmentRequest.getQuantity());

        recordMovement(
                adjustmentRequest.getProductId(),
                adjustmentRequest.getQuantity(),
                adjustmentRequest.getMovementType(),
                adjustmentRequest.getReason()
        );

        // Get the latest inventory record
        List<Inventory> movements = inventoryRepository.findByProductId(adjustmentRequest.getProductId());
        Inventory latestMovement = movements.get(movements.size() - 1);

        return convertToResponseDTO(latestMovement);
    }

    /**
     * Get stock history for a product
     */
    public List<InventoryResponseDTO> getStockHistory(Long productId) {
        logger.info("Fetching stock history for product: {}", productId);

        // Verify product exists
        productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        return inventoryRepository.findProductHistoryOrderByDateDesc(productId).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get low stock products
     */
    public List<InventoryResponseDTO> getLowStockAlerts() {
        logger.info("Fetching low stock products");

        List<Product> lowStockProducts = productRepository.findLowStockProducts();

        return lowStockProducts.stream()
                .flatMap(product -> inventoryRepository.findProductHistoryOrderByDateDesc(product.getId()).stream().limit(1))
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get current stock level
     */
    public Integer getStockLevel(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        return product.getCurrentStock();
    }

    /**
     * Calculate new stock based on movement type
     */
    private Integer calculateNewStock(Integer currentStock, Integer quantity, MovementType movementType) {
        switch (movementType) {
            case PURCHASE:
            case RETURN:
                return currentStock + quantity;
            case SALE:
            case DAMAGE:
            case ADJUSTMENT:
                return currentStock - quantity;
            default:
                return currentStock;
        }
    }

    /**
     * Get current user
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
    }

    /**
     * Convert Inventory entity to InventoryResponseDTO
     */
    private InventoryResponseDTO convertToResponseDTO(Inventory inventory) {
        return InventoryResponseDTO.builder()
                .id(inventory.getId())
                .productId(inventory.getProduct().getId())
                .productName(inventory.getProduct().getName())
                .movementType(inventory.getMovementType())
                .quantity(inventory.getQuantity())
                .previousStock(inventory.getPreviousStock())
                .newStock(inventory.getNewStock())
                .reason(inventory.getReason())
                .createdBy(inventory.getCreatedBy() != null ? inventory.getCreatedBy().getUsername() : null)
                .createdAt(inventory.getCreatedAt())
                .build();
    }
}