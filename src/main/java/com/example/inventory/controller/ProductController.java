package com.example.inventory.controller;

import com.example.inventory.dto.ProductRequestDTO;
import com.example.inventory.dto.ProductResponseDTO;
import com.example.inventory.dto.ApiResponseDTO;
import com.example.inventory.service.ProductService;
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
@RequestMapping("/api/products")
@Tag(name = "Products", description = "Product management endpoints")
@CrossOrigin(origins = "*", maxAge = 3600)
@SecurityRequirement(name = "Bearer Authentication")
public class ProductController {

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    @Autowired
    private ProductService productService;

    /**
     * Add new product (Admin only)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Add new product", description = "Create a new product (Admin only)")
    public ResponseEntity<ApiResponseDTO<ProductResponseDTO>> addProduct(@Valid @RequestBody ProductRequestDTO productRequest) {
        logger.info("Adding new product: {}", productRequest.getSku());

        ProductResponseDTO product = productService.addProduct(productRequest);

        ApiResponseDTO<ProductResponseDTO> response = ApiResponseDTO.<ProductResponseDTO>builder()
                .success(true)
                .message("Product added successfully")
                .data(product)
                .statusCode(201)
                .timestamp(System.currentTimeMillis())
                .build();

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Get all active products
     */
    @GetMapping
    @Operation(summary = "Get all products", description = "Retrieve all active products")
    public ResponseEntity<ApiResponseDTO<List<ProductResponseDTO>>> getAllProducts() {
        logger.info("Fetching all active products");

        List<ProductResponseDTO> products = productService.getAllActiveProducts();

        ApiResponseDTO<List<ProductResponseDTO>> response = ApiResponseDTO.<List<ProductResponseDTO>>builder()
                .success(true)
                .message("Products retrieved successfully")
                .data(products)
                .statusCode(200)
                .timestamp(System.currentTimeMillis())
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Get product by ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID", description = "Retrieve a specific product by ID")
    public ResponseEntity<ApiResponseDTO<ProductResponseDTO>> getProductById(@PathVariable Long id) {
        logger.info("Fetching product with ID: {}", id);

        ProductResponseDTO product = productService.getProductById(id);

        ApiResponseDTO<ProductResponseDTO> response = ApiResponseDTO.<ProductResponseDTO>builder()
                .success(true)
                .message("Product retrieved successfully")
                .data(product)
                .statusCode(200)
                .timestamp(System.currentTimeMillis())
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Get product by SKU
     */
    @GetMapping("/sku/{sku}")
    @Operation(summary = "Get product by SKU", description = "Retrieve a product by its SKU")
    public ResponseEntity<ApiResponseDTO<ProductResponseDTO>> getProductBySku(@PathVariable String sku) {
        logger.info("Fetching product with SKU: {}", sku);

        ProductResponseDTO product = productService.getProductBySku(sku);

        ApiResponseDTO<ProductResponseDTO> response = ApiResponseDTO.<ProductResponseDTO>builder()
                .success(true)
                .message("Product retrieved successfully")
                .data(product)
                .statusCode(200)
                .timestamp(System.currentTimeMillis())
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Get products by category
     */
    @GetMapping("/category/{category}")
    @Operation(summary = "Get products by category", description = "Retrieve all products in a specific category")
    public ResponseEntity<ApiResponseDTO<List<ProductResponseDTO>>> getProductsByCategory(@PathVariable String category) {
        logger.info("Fetching products with category: {}", category);

        List<ProductResponseDTO> products = productService.getProductsByCategory(category);

        ApiResponseDTO<List<ProductResponseDTO>> response = ApiResponseDTO.<List<ProductResponseDTO>>builder()
                .success(true)
                .message("Products retrieved successfully")
                .data(products)
                .statusCode(200)
                .timestamp(System.currentTimeMillis())
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Update product (Admin only)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update product", description = "Update an existing product (Admin only)")
    public ResponseEntity<ApiResponseDTO<ProductResponseDTO>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequestDTO productRequest) {
        logger.info("Updating product with ID: {}", id);

        ProductResponseDTO product = productService.updateProduct(id, productRequest);

        ApiResponseDTO<ProductResponseDTO> response = ApiResponseDTO.<ProductResponseDTO>builder()
                .success(true)
                .message("Product updated successfully")
                .data(product)
                .statusCode(200)
                .timestamp(System.currentTimeMillis())
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Delete product (Admin only)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete product", description = "Delete a product (Admin only)")
    public ResponseEntity<ApiResponseDTO<?>> deleteProduct(@PathVariable Long id) {
        logger.info("Deleting product with ID: {}", id);

        productService.deleteProduct(id);

        ApiResponseDTO<?> response = ApiResponseDTO.builder()
                .success(true)
                .message("Product deleted successfully")
                .statusCode(200)
                .timestamp(System.currentTimeMillis())
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Get low stock products (Admin/Warehouse Staff only)
     */
    @GetMapping("/stock/low")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_STAFF')")
    @Operation(summary = "Get low stock products", description = "Retrieve all products with low stock")
    public ResponseEntity<ApiResponseDTO<List<ProductResponseDTO>>> getLowStockProducts() {
        logger.info("Fetching low stock products");

        List<ProductResponseDTO> products = productService.getLowStockProducts();

        ApiResponseDTO<List<ProductResponseDTO>> response = ApiResponseDTO.<List<ProductResponseDTO>>builder()
                .success(true)
                .message("Low stock products retrieved successfully")
                .data(products)
                .statusCode(200)
                .timestamp(System.currentTimeMillis())
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}