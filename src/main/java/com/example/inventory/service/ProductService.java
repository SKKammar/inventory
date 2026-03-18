package com.example.inventory.service;

import com.example.inventory.dto.ProductRequestDTO;
import com.example.inventory.dto.ProductResponseDTO;
import com.example.inventory.entity.Product;
import com.example.inventory.exception.DuplicateResourceException;
import com.example.inventory.exception.ResourceNotFoundException;
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
public class ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    @Autowired
    private ProductRepository productRepository;

    public ProductResponseDTO addProduct(ProductRequestDTO req) {
        if (productRepository.existsBySku(req.getSku()))
            throw new DuplicateResourceException("Product", "sku", req.getSku());

        Product product = Product.builder()
                .name(req.getName()).description(req.getDescription())
                .sku(req.getSku()).category(req.getCategory())
                .price(req.getPrice()).currentStock(req.getCurrentStock())
                .minStockLevel(req.getMinStockLevel()).maxStockLevel(req.getMaxStockLevel())
                .unit(req.getUnit()).isActive(true).build();

        return toDTO(productRepository.save(product));
    }

    public ProductResponseDTO getProductById(Long id) {
        return toDTO(productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id)));
    }

    public ProductResponseDTO getProductBySku(String sku) {
        return toDTO(productRepository.findBySku(sku)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "sku", sku)));
    }

    public List<ProductResponseDTO> getAllActiveProducts() {
        return productRepository.findByIsActiveTrue().stream()
                .map(this::toDTO).collect(Collectors.toList());
    }

    public List<ProductResponseDTO> getProductsByCategory(String category) {
        return productRepository.findByCategoryAndIsActiveTrue(category).stream()
                .map(this::toDTO).collect(Collectors.toList());
    }

    public ProductResponseDTO updateProduct(Long id, ProductRequestDTO req) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        if (!product.getSku().equals(req.getSku()) && productRepository.existsBySku(req.getSku()))
            throw new DuplicateResourceException("Product", "sku", req.getSku());

        product.setName(req.getName()); product.setDescription(req.getDescription());
        product.setSku(req.getSku()); product.setCategory(req.getCategory());
        product.setPrice(req.getPrice()); product.setCurrentStock(req.getCurrentStock());
        product.setMinStockLevel(req.getMinStockLevel()); product.setMaxStockLevel(req.getMaxStockLevel());
        product.setUnit(req.getUnit());
        return toDTO(productRepository.save(product));
    }

    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        product.setIsActive(false);
        productRepository.save(product);
    }

    public List<ProductResponseDTO> getLowStockProducts() {
        return productRepository.findLowStockProducts().stream()
                .map(this::toDTO).collect(Collectors.toList());
    }

    public boolean isStockAvailable(Long productId, Integer required) {
        Product p = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
        return p.getCurrentStock() >= required;
    }

    public Integer getCurrentStock(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId))
                .getCurrentStock();
    }

    protected void updateStock(Long productId, Integer newStock) {
        Product p = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
        p.setCurrentStock(newStock);
        productRepository.save(p);
    }

    private ProductResponseDTO toDTO(Product p) {
        boolean isLowStock = p.getMinStockLevel() != null && p.getCurrentStock() < p.getMinStockLevel();
        return ProductResponseDTO.builder()
                .id(p.getId()).name(p.getName()).description(p.getDescription())
                .sku(p.getSku()).category(p.getCategory()).price(p.getPrice())
                .currentStock(p.getCurrentStock()).minStockLevel(p.getMinStockLevel())
                .maxStockLevel(p.getMaxStockLevel()).unit(p.getUnit())
                .isActive(p.getIsActive()).isLowStock(isLowStock)
                .createdAt(p.getCreatedAt()).updatedAt(p.getUpdatedAt())
                .build();
    }
}
