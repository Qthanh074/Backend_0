package org.example.backend9.repository.inventory;

import org.example.backend9.entity.inventory.ReturnTicketDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReturnTicketDetailRepository extends JpaRepository<ReturnTicketDetail, Integer> {
    List<ReturnTicketDetail> findByReturnTicketId(Integer ticketId);
}