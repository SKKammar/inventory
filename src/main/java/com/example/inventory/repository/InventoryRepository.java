package com.example.inventory.repository;

import com.example.inventory.entity.Inventory;
import com.example.inventory.entity.Product;
import com.example.inventory.entity.Inventory.MovementType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    List<Inventory> findByProduct(Product product);
    List<Inventory> findByProductId(Long productId);
    List<Inventory> findByMovementType(MovementType movementType);

    @Query("SELECT i FROM Inventory i WHERE i.product.id = :productId ORDER BY i.createdAt DESC")
    List<Inventory> findProductHistoryOrderByDateDesc(Long productId);

    @Query("SELECT i FROM Inventory i WHERE i.product.id = :productId AND i.createdAt BETWEEN :startDate AND :endDate")
    List<Inventory> findByProductIdAndDateRange(Long productId, LocalDateTime startDate, LocalDateTime endDate);
}