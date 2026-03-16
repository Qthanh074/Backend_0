package org.example.backend9.repository.inventory;
import org.example.backend9.entity.inventory.TransferTicket;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransferTicketRepository extends JpaRepository<TransferTicket, Integer> {}