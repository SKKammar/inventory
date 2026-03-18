package com.example.inventory.repository;
import com.example.inventory.entity.Inventory;
import com.example.inventory.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    List<Inventory> findByProductOrderByCreatedAtDesc(Product product);
}
