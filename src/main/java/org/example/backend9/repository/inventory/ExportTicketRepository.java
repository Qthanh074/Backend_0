package org.example.backend9.repository.inventory;

import org.example.backend9.entity.inventory.ExportTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExportTicketRepository extends JpaRepository<ExportTicket, Integer> {
    boolean existsByCode(String code);
}