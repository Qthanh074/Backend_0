package org.example.backend9.repository.inventory;

import org.example.backend9.entity.inventory.ImportTicket;
import org.example.backend9.enums.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ImportTicketRepository extends JpaRepository<ImportTicket, Integer> {
    boolean existsByCode(String code);
    List<ImportTicket> findBySupplierIdAndStatusOrderByImportDateAsc(Integer supplierId, TicketStatus status);
}