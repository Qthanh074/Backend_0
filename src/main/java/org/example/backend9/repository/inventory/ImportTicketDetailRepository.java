package org.example.backend9.repository.inventory;

import org.example.backend9.entity.inventory.ImportTicketDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ImportTicketDetailRepository extends JpaRepository<ImportTicketDetail, Integer> {
    List<ImportTicketDetail> findByImportTicketId(Integer importTicketId);
}