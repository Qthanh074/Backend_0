package org.example.backend9.repository.sales;

import org.example.backend9.entity.sales.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    Optional<Order> findByOrderNumber(String orderNumber);
}

// OrderDetailRepository thường không cần viết nhiều vì ta thao tác qua Order (Cascade)