package org.example.backend9.repository.inventory;

import org.example.backend9.entity.inventory.ImportTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImportTicketRepository extends JpaRepository<ImportTicket, Integer> {
    boolean existsByCode(String code);
}