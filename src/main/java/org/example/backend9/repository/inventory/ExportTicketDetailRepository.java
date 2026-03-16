package org.example.backend9.repository.inventory;

import org.example.backend9.entity.inventory.ExportTicketDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ExportTicketDetailRepository extends JpaRepository<ExportTicketDetail, Integer> {
    List<ExportTicketDetail> findByExportTicketId(Integer exportTicketId);
}