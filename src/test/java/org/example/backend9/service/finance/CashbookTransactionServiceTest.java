package org.example.backend9.service.finance;

import org.example.backend9.dto.request.finance.CashbookTransactionRequest;
import org.example.backend9.dto.request.finance.SupplierDebtPaymentRequest;
import org.example.backend9.dto.response.finance.CashbookTransactionResponse;
import org.example.backend9.entity.core.Employee;
import org.example.backend9.entity.core.Store;
import org.example.backend9.entity.core.Supplier;
import org.example.backend9.entity.finance.CashbookTransaction;
import org.example.backend9.enums.PaymentMethod;
import org.example.backend9.enums.TransactionType;
import org.example.backend9.repository.core.EmployeeRepository;
import org.example.backend9.repository.core.StoreRepository;
import org.example.backend9.repository.core.SupplierRepository;
import org.example.backend9.repository.finance.CashbookTransactionRepository;
import org.example.backend9.service.GoogleSheetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CashbookTransactionServiceTest {

    @Mock private CashbookTransactionRepository cashbookRepository;
    @Mock private StoreRepository storeRepository;
    @Mock private EmployeeRepository employeeRepository;
    @Mock private SupplierRepository supplierRepository;
    @Mock private GoogleSheetService googleSheetService;

    @InjectMocks
    private CashbookTransactionService cashbookService;

    private Store mockStore;
    private Employee mockEmployee;

    @BeforeEach
    void setUp() {
        // Fix lỗi Integer: Không dùng 1L, dùng số nguyên bình thường
        mockStore = new Store();
        mockStore.setId(1);
        mockStore.setName("Cửa hàng Hà Nội");

        mockEmployee = new Employee();
        mockEmployee.setId(1);
        mockEmployee.setFullName("Ngọc Admin");
    }

    @Test
    @DisplayName("1. Create: Thu tiền (INCOME) - Số dư tăng")
    void createTransaction_Income_Success() {
        CashbookTransactionRequest req = new CashbookTransactionRequest();
        req.setStoreId(1);
        req.setCreatorId(1);
        req.setType(TransactionType.INCOME);
        req.setMethod(PaymentMethod.CASH);
        req.setAmount(new BigDecimal("100000"));

        CashbookTransaction lastTx = new CashbookTransaction();
        lastTx.setBalanceAfterTransaction(new BigDecimal("50000"));

        when(storeRepository.findById(1)).thenReturn(Optional.of(mockStore));
        when(employeeRepository.findById(1)).thenReturn(Optional.of(mockEmployee));
        when(cashbookRepository.findTopByMethodOrderByTransactionDateDesc(PaymentMethod.CASH))
                .thenReturn(Optional.of(lastTx));
        when(cashbookRepository.save(any(CashbookTransaction.class))).thenAnswer(i -> i.getArgument(0));

        CashbookTransactionResponse res = cashbookService.createTransaction(req);

        assertNotNull(res);
        assertEquals(new BigDecimal("150000"), res.getBalanceAfterTransaction());
        verify(googleSheetService).appendRowToSheet(anyString(), anyList());
    }

    @Test
    @DisplayName("2. Create: Chi tiền (EXPENSE) - Số dư giảm")
    void createTransaction_Expense_Success() {
        CashbookTransactionRequest req = new CashbookTransactionRequest();
        req.setStoreId(1);
        req.setCreatorId(1);
        req.setType(TransactionType.EXPENSE);
        req.setMethod(PaymentMethod.BANK_TRANSFER);
        req.setAmount(new BigDecimal("20000"));

        CashbookTransaction lastTx = new CashbookTransaction();
        lastTx.setBalanceAfterTransaction(new BigDecimal("100000"));

        when(storeRepository.findById(1)).thenReturn(Optional.of(mockStore));
        when(employeeRepository.findById(1)).thenReturn(Optional.of(mockEmployee));
        when(cashbookRepository.findTopByMethodOrderByTransactionDateDesc(PaymentMethod.BANK_TRANSFER))
                .thenReturn(Optional.of(lastTx));
        when(cashbookRepository.save(any(CashbookTransaction.class))).thenAnswer(i -> i.getArgument(0));

        CashbookTransactionResponse res = cashbookService.createTransaction(req);

        assertEquals(new BigDecimal("80000"), res.getBalanceAfterTransaction());
    }

    @Test
    @DisplayName("3. PaySupplier: Trả nợ NCC thành công")
    void paySupplierDebt_Success() {
        Supplier supplier = new Supplier();
        supplier.setId(10);
        supplier.setName("NCC Tổng");
        supplier.setDebt(500000.0);

        SupplierDebtPaymentRequest req = new SupplierDebtPaymentRequest();
        req.setSupplierId(10);
        req.setAmount(new BigDecimal("200000"));
        req.setStoreId(1);
        req.setCreatorId(1);
        req.setMethod(PaymentMethod.CASH);

        when(supplierRepository.findById(10)).thenReturn(Optional.of(supplier));
        when(storeRepository.findById(1)).thenReturn(Optional.of(mockStore));
        when(employeeRepository.findById(1)).thenReturn(Optional.of(mockEmployee));
        when(cashbookRepository.findTopByMethodOrderByTransactionDateDesc(any())).thenReturn(Optional.empty());
        when(cashbookRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        cashbookService.paySupplierDebt(req);

        assertEquals(300000.0, supplier.getDebt());
        verify(supplierRepository).save(supplier);
    }

    @Test
    @DisplayName("4. Error: Ném lỗi khi không tìm thấy dữ liệu")
    void create_Error_NotFound() {
        when(storeRepository.findById(anyInt())).thenReturn(Optional.empty());

        CashbookTransactionRequest req = new CashbookTransactionRequest();
        req.setStoreId(99);

        assertThrows(RuntimeException.class, () -> cashbookService.createTransaction(req));
    }

    @Test
    @DisplayName("5. Coverage: Test getCurrentBalance")
    void test_GetCurrentBalance() {
        CashbookTransaction tx = new CashbookTransaction();
        tx.setBalanceAfterTransaction(new BigDecimal("500.0"));

        when(cashbookRepository.findTopByMethodOrderByTransactionDateDesc(PaymentMethod.CASH))
                .thenReturn(Optional.of(tx));

        BigDecimal balance = cashbookService.getCurrentBalance(PaymentMethod.CASH);
        assertEquals(new BigDecimal("500.0"), balance);
    }

    @Test
    @DisplayName("6. Coverage: Test getTransactions list")
    void test_GetTransactions() {
        when(cashbookRepository.filterTransactions(null, null, null))
                .thenReturn(Collections.emptyList());

        List<CashbookTransactionResponse> list = cashbookService.getTransactions(null, null, null);
        assertTrue(list.isEmpty());
    }

    @Test
    @DisplayName("7. Coverage: Test Sync Google Sheet bị lỗi (Try-Catch)")
    void test_SyncGoogleSheet_Error() {
        // Tạo một response giả
        CashbookTransactionResponse res = new CashbookTransactionResponse();
        res.setCode("PT01");
        res.setTransactionDate(java.time.LocalDateTime.now());
        res.setType(TransactionType.INCOME);

        // Giả lập GoogleSheetService ném lỗi
        doThrow(new RuntimeException("API Error")).when(googleSheetService).appendRowToSheet(anyString(), anyList());

        // Gọi hàm private thông qua hàm public createTransaction hoặc test riêng nếu cần
        // Ở đây ta chỉ cần đảm bảo code không sập khi catch error
        assertDoesNotThrow(() -> {
            // Test nhánh catch trong syncToGoogleSheet bằng cách tạo 1 transaction
            createTransaction_Income_Success();
        });
    }
}