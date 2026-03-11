package org.example.backend9.repository.core;

import org.example.backend9.entity.core.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Integer> {
    Optional<Employee> findByEmail(String email);
    Optional<Employee> findByVerificationToken(String token);
    boolean existsByEmail(String email);
}