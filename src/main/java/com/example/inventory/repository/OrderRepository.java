package com.example.inventory.repository;
import com.example.inventory.entity.Order;
import com.example.inventory.entity.Order.OrderStatus;
import com.example.inventory.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderNumber(String orderNumber);
    List<Order> findByStatus(OrderStatus status);
    @Query("SELECT o FROM Order o WHERE o.user = :user ORDER BY o.orderDate DESC")
    List<Order> findUserOrdersOrderByDateDesc(@Param("user") User user);
    @Query("SELECT o FROM Order o WHERE o.orderDate BETWEEN :start AND :end")
    List<Order> findOrdersByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
