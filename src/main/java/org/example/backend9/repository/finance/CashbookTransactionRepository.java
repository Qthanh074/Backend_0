package org.example.backend9.repository.finance;

import org.example.backend9.entity.finance.CashbookTransaction;
import org.example.backend9.enums.PaymentMethod;
import org.example.backend9.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CashbookTransactionRepository extends JpaRepository<CashbookTransaction, Integer> {

    // Lấy số dư gần nhất để tính tồn quỹ
    Optional<CashbookTransaction> findTopByStoreIdOrderByTransactionDateDesc(Integer storeId);

    // Query siêu cấp phục vụ tất cả 4 trang UI (Tiền mặt, Ngân hàng, Phiếu Thu, Phiếu Chi)
    @Query("SELECT c FROM CashbookTransaction c WHERE " +
            "(:type IS NULL OR c.type = :type) AND " +
            "(:method IS NULL OR c.method = :method) AND " +
            "(:search IS NULL OR c.code LIKE %:search% OR c.description LIKE %:search% OR c.referenceName LIKE %:search%) " +
            "ORDER BY c.transactionDate DESC")
    List<CashbookTransaction> filterTransactions(
            @Param("type") TransactionType type,
            @Param("method") PaymentMethod method,
            @Param("search") String search
    );
}