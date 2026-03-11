package org.example.backend9.repository.inventory;

import org.example.backend9.entity.inventory.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SizeRepository extends JpaRepository<Size, Long> {
    boolean existsByName(String name);
}