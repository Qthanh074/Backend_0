package org.example.backend9.repository.sales;

import org.example.backend9.entity.sales.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer> {

    // 👉 Thêm dòng này để Spring Data JPA tự sinh câu lệnh tìm kiếm theo Tên hoặc SĐT
    List<Customer> findByFullNameContainingOrPhoneContaining(String fullName, String phone);

    boolean existsByCode(String code);
    boolean existsByPhone(String phone);
}