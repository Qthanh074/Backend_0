package org.example.backend9.repository.inventory;
import org.example.backend9.entity.inventory.InventoryCheck;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryCheckRepository extends JpaRepository<InventoryCheck, Integer> {}