package org.example.backend9.repository.inventory;
import org.example.backend9.entity.inventory.InventoryCheckDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InventoryCheckDetailRepository extends JpaRepository<InventoryCheckDetail, Integer> {
    List<InventoryCheckDetail> findByInventoryCheckId(Integer checkId);
}