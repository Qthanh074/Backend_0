package org.example.backend9.repository.sales;

import org.example.backend9.entity.sales.Order;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    Optional<Order> findByOrderNumber(String orderNumber);
    @Override
    @EntityGraph(attributePaths = {
            "customer",
            "employee",
            "store",
            "orderDetails",
            "orderDetails.productVariant",
            "orderDetails.productVariant.product"
    })
    List<Order> findAll();
}

// OrderDetailRepository thường không cần viết nhiều vì ta thao tác qua Order (Cascade)