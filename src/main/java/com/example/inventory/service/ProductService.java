package com.example.inventory.service;

import com.example.inventory.dto.ProductRequestDTO;
import com.example.inventory.dto.ProductResponseDTO;
import com.example.inventory.entity.Product;
import com.example.inventory.exception.DuplicateResourceException;
import com.example.inventory.exception.ResourceNotFoundException;
import com.example.inventory.repository.ProductRepository;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ModelMapper modelMapper;

    /**
     * Add new product
     */
    public ProductResponseDTO addProduct(ProductRequestDTO productRequest) {
        logger.info("Adding new product: {}", productRequest.getSku());

        // Check if SKU already exists
        if (productRepository.existsBySku(productRequest.getSku())) {
            throw new DuplicateResourceException("Product", "sku", productRequest.getSku());
        }

        Product product = Product.builder()
                .name(productRequest.getName())
                .description(productRequest.getDescription())
                .sku(productRequest.getSku())
                .category(productRequest.getCategory())
                .price(productRequest.getPrice())
                .currentStock(productRequest.getCurrentStock())
                .minStockLevel(productRequest.getMinStockLevel())
                .maxStockLevel(productRequest.getMaxStockLevel())
                .unit(productRequest.getUnit())
                .isActive(true)
                .build();

        Product savedProduct = productRepository.save(product);
        logger.info("Product added successfully: {}", savedProduct.getId());

        return convertToResponseDTO(savedProduct);
    }

    /**
     * Get product by ID
     */
    public ProductResponseDTO getProductById(Long productId) {
        logger.info("Fetching product with ID: {}", productId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        return convertToResponseDTO(product);
    }

    /**
     * Get product by SKU
     */
    public ProductResponseDTO getProductBySku(String sku) {
        logger.info("Fetching product with SKU: {}", sku);

        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "sku", sku));

        return convertToResponseDTO(product);
    }

    /**
     * Get all active products
     */
    public List<ProductResponseDTO> getAllActiveProducts() {
        logger.info("Fetching all active products");

        return productRepository.findByIsActiveTrue().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get products by category
     */
    public List<ProductResponseDTO> getProductsByCategory(String category) {
        logger.info("Fetching products by category: {}", category);

        return productRepository.findByCategoryAndIsActiveTrue(category).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Update product
     */
    public ProductResponseDTO updateProduct(Long productId, ProductRequestDTO productRequest) {
        logger.info("Updating product with ID: {}", productId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        // Check if new SKU already exists (if changed)
        if (!product.getSku().equals(productRequest.getSku()) &&
                productRepository.existsBySku(productRequest.getSku())) {
            throw new DuplicateResourceException("Product", "sku", productRequest.getSku());
        }

        product.setName(productRequest.getName());
        product.setDescription(productRequest.getDescription());
        product.setSku(productRequest.getSku());
        product.setCategory(productRequest.getCategory());
        product.setPrice(productRequest.getPrice());
        product.setCurrentStock(productRequest.getCurrentStock());
        product.setMinStockLevel(productRequest.getMinStockLevel());
        product.setMaxStockLevel(productRequest.getMaxStockLevel());
        product.setUnit(productRequest.getUnit());

        Product updatedProduct = productRepository.save(product);
        logger.info("Product updated successfully: {}", productId);

        return convertToResponseDTO(updatedProduct);
    }

    /**
     * Delete product
     */
    public void deleteProduct(Long productId) {
        logger.info("Deleting product with ID: {}", productId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        product.setIsActive(false);
        productRepository.save(product);
        logger.info("Product deleted (deactivated) successfully: {}", productId);
    }

    /**
     * Get low stock products
     */
    public List<ProductResponseDTO> getLowStockProducts() {
        logger.info("Fetching low stock products");

        return productRepository.findLowStockProducts().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Check stock availability
     */
    public boolean isStockAvailable(Long productId, Integer requiredQuantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        return product.getCurrentStock() >= requiredQuantity;
    }

    /**
     * Get current stock
     */
    public Integer getCurrentStock(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        return product.getCurrentStock();
    }

    /**
     * Update stock (internal use)
     */
    protected void updateStock(Long productId, Integer newStock) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        product.setCurrentStock(newStock);
        productRepository.save(product);
    }

    /**
     * Convert Product entity to ProductResponseDTO
     */
    private ProductResponseDTO convertToResponseDTO(Product product) {
        ProductResponseDTO dto = ProductResponseDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .sku(product.getSku())
                .category(product.getCategory())
                .price(product.getPrice())
                .currentStock(product.getCurrentStock())
                .minStockLevel(product.getMinStockLevel())
                .maxStockLevel(product.getMaxStockLevel())
                .unit(product.getUnit())
                .isActive(product.getIsActive())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();

        // Check if stock is low
        if (product.getMinStockLevel() != null &&
                product.getCurrentStock() < product.getMinStockLevel()) {
            dto.setIsLowStock(true);
        } else {
            dto.setIsLowStock(false);
        }

        return dto;
    }
}