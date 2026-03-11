package org.example.backend9.repository.inventory;

import org.example.backend9.entity.inventory.Unit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UnitRepository extends JpaRepository<Unit, Long> {
    boolean existsByName(String name);
}