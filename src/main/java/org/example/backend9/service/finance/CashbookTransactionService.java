package org.example.backend9.service.finance;

import org.example.backend9.dto.request.finance.CashbookTransactionRequest;
import org.example.backend9.dto.request.finance.SupplierDebtPaymentRequest;
import org.example.backend9.dto.response.finance.CashbookTransactionResponse;
import org.example.backend9.entity.core.Employee;
import org.example.backend9.entity.core.Store;
import org.example.backend9.entity.core.Supplier;
import org.example.backend9.entity.finance.CashbookTransaction;
import org.example.backend9.enums.PaymentMethod;
import org.example.backend9.enums.TicketStatus;
import org.example.backend9.enums.TransactionType;
import org.example.backend9.repository.core.EmployeeRepository;
import org.example.backend9.repository.core.StoreRepository;
import org.example.backend9.repository.core.SupplierRepository;
import org.example.backend9.repository.finance.CashbookTransactionRepository;
import org.example.backend9.service.GoogleSheetService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CashbookTransactionService {

    private final CashbookTransactionRepository cashbookRepository;
    private final StoreRepository storeRepository;
    private final EmployeeRepository employeeRepository;
    private final SupplierRepository supplierRepository;
    private final GoogleSheetService googleSheetService;

    public CashbookTransactionService(CashbookTransactionRepository cashbookRepository,
                                      StoreRepository storeRepository,
                                      EmployeeRepository employeeRepository,
                                      SupplierRepository supplierRepository,
                                      GoogleSheetService googleSheetService) {
        this.cashbookRepository = cashbookRepository;
        this.storeRepository = storeRepository;
        this.employeeRepository = employeeRepository;
        this.supplierRepository = supplierRepository;
        this.googleSheetService = googleSheetService;
    }

    // 👉 ĐÂY LÀ HÀM BẠN ĐANG THIẾU (Fix lỗi Cannot find symbol)
    public BigDecimal getCurrentBalance(PaymentMethod method) {
        return cashbookRepository.findTopByMethodOrderByTransactionDateDesc(method)
                .map(CashbookTransaction::getBalanceAfterTransaction)
                .orElse(BigDecimal.ZERO);
    }

    @Transactional
    public CashbookTransactionResponse createTransaction(CashbookTransactionRequest request) {
        Store store = storeRepository.findById(request.getStoreId())
                .orElseThrow(() -> new RuntimeException("Cửa hàng không tồn tại"));
        Employee creator = employeeRepository.findById(request.getCreatorId())
                .orElseThrow(() -> new RuntimeException("Nhân viên không tồn tại"));

        CashbookTransaction transaction = new CashbookTransaction();
        String prefix = (request.getType() == TransactionType.INCOME ? "PT" : "PC");
        transaction.setCode(prefix + System.currentTimeMillis() % 1000000);
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setType(request.getType());
        transaction.setMethod(request.getMethod());
        transaction.setCategory(request.getCategory());
        transaction.setDescription(request.getDescription());
        transaction.setReferenceName(request.getReferenceName());
        transaction.setAmount(request.getAmount());
        transaction.setStore(store);
        transaction.setCreator(creator);
        transaction.setStatus(TicketStatus.COMPLETED);

        // 💡 Sửa logic: Lấy số dư cuối của ĐÚNG phương thức thanh toán này (Cash hoặc Bank)
        BigDecimal lastBalance = cashbookRepository.findTopByMethodOrderByTransactionDateDesc(request.getMethod())
                .map(CashbookTransaction::getBalanceAfterTransaction)
                .orElse(BigDecimal.ZERO);

        BigDecimal newBalance = request.getType() == TransactionType.INCOME
                ? lastBalance.add(request.getAmount())
                : lastBalance.subtract(request.getAmount());

        transaction.setBalanceAfterTransaction(newBalance);

        CashbookTransaction saved = cashbookRepository.save(transaction);
        CashbookTransactionResponse response = mapToResponse(saved);

        syncToGoogleSheet(response);

        return response;
    }

    @Transactional
    public CashbookTransactionResponse paySupplierDebt(SupplierDebtPaymentRequest request) {
        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Nhà cung cấp"));

        CashbookTransactionRequest txReq = new CashbookTransactionRequest();
        txReq.setType(TransactionType.EXPENSE);
        txReq.setMethod(request.getMethod());
        txReq.setCategory("Trả nợ nhà cung cấp");
        txReq.setDescription(request.getNotes() != null ? request.getNotes() : "Thanh toán nợ: " + supplier.getName());
        txReq.setReferenceName(supplier.getName());
        txReq.setAmount(request.getAmount());
        txReq.setStoreId(request.getStoreId());
        txReq.setCreatorId(request.getCreatorId());

        CashbookTransactionResponse savedTx = createTransaction(txReq);
        double currentDebt = supplier.getDebt();
        double payAmount = request.getAmount().doubleValue();

        if (currentDebt > 0) {
            // Nếu nợ đang là số dương (ví dụ 500k), thì TRỪ đi
            supplier.setDebt(currentDebt - payAmount);
        } else {
            // Nếu nợ đang lưu số âm (ví dụ -500k), thì CỘNG vào để nó về 0
            supplier.setDebt(currentDebt + payAmount);
        }

        supplierRepository.save(supplier);
        return savedTx;


    }

    private void syncToGoogleSheet(CashbookTransactionResponse res) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            List<Object> rowData = Arrays.asList(
                    res.getCode(),
                    res.getTransactionDate().format(formatter),
                    res.getType().toString(),
                    res.getCategory(),
                    res.getAmount().doubleValue(),
                    res.getBalanceAfterTransaction().doubleValue(),
                    res.getReferenceName(),
                    res.getStoreName(),
                    res.getCreatorName(),
                    res.getDescription()
            );
            googleSheetService.appendRowToSheet("Transactions", rowData);
        } catch (Exception e) {
            System.err.println("Google Sheet Sync Error: " + e.getMessage());
        }
    }

    public List<CashbookTransactionResponse> getTransactions(TransactionType type, PaymentMethod method, String search) {
        return cashbookRepository.filterTransactions(type, method, search)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    private CashbookTransactionResponse mapToResponse(CashbookTransaction transaction) {
        CashbookTransactionResponse res = new CashbookTransactionResponse();
        res.setId(transaction.getId());
        res.setCode(transaction.getCode());
        res.setTransactionDate(transaction.getTransactionDate());
        res.setType(transaction.getType());
        res.setMethod(transaction.getMethod());
        res.setCategory(transaction.getCategory());
        res.setDescription(transaction.getDescription());
        res.setReferenceName(transaction.getReferenceName());
        res.setAmount(transaction.getAmount());
        res.setBalanceAfterTransaction(transaction.getBalanceAfterTransaction());
        res.setStatus(transaction.getStatus());
        if (transaction.getStore() != null) res.setStoreName(transaction.getStore().getName());
        if (transaction.getCreator() != null) res.setCreatorName(transaction.getCreator().getFullName());
        return res;
    }

}