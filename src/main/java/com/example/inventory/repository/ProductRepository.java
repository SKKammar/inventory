package com.example.inventory.repository;
import com.example.inventory.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;
public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findBySku(String sku);
    boolean existsBySku(String sku);
    List<Product> findByIsActiveTrue();
    List<Product> findByCategoryAndIsActiveTrue(String category);
    @Query("SELECT p FROM Product p WHERE p.isActive = true AND p.minStockLevel IS NOT NULL AND p.currentStock < p.minStockLevel")
    List<Product> findLowStockProducts();
}
