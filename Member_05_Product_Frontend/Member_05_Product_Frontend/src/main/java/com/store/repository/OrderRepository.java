package com.store.repository;

import com.store.entity.Order;
import com.store.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    
    // Find all orders that belong to a specific user
    // SELECT * FROM orders WHERE user_id = ?
    List<Order> findByUser(User user);
}
