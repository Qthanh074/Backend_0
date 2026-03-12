package org.example.backend9.repository.sales;

import org.example.backend9.entity.sales.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer> {
    boolean existsByCode(String code);
    boolean existsByPhone(String phone);
}