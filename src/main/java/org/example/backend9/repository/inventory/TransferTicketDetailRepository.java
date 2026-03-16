package org.example.backend9.repository.inventory;
import org.example.backend9.entity.inventory.TransferTicketDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TransferTicketDetailRepository extends JpaRepository<TransferTicketDetail, Integer> {
    List<TransferTicketDetail> findByTransferTicketId(Integer ticketId);
}