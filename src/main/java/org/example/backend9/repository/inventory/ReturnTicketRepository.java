package org.example.backend9.repository.inventory;

import org.example.backend9.entity.inventory.ReturnTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReturnTicketRepository extends JpaRepository<ReturnTicket, Integer> {
    List<ReturnTicket> findByReturnType(String returnType);
}