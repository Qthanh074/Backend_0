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
import org.example.backend9.service.GoogleSheetService; // Import service của bạn
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
    private final GoogleSheetService googleSheetService; // Tiêm GoogleSheetService

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

    // --- LOGIC CHÍNH: TẠO GIAO DỊCH ---
    @Transactional
    public CashbookTransactionResponse createTransaction(CashbookTransactionRequest request) {
        Store store = storeRepository.findById(request.getStoreId())
                .orElseThrow(() -> new RuntimeException("Cửa hàng không tồn tại"));
        Employee creator = employeeRepository.findById(request.getCreatorId())
                .orElseThrow(() -> new RuntimeException("Nhân viên không tồn tại"));

        // 1. Khởi tạo thực thể
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

        // 2. Tính toán số dư sau giao dịch
        BigDecimal lastBalance = cashbookRepository.findTopByStoreIdOrderByTransactionDateDesc(store.getId())
                .map(CashbookTransaction::getBalanceAfterTransaction)
                .orElse(BigDecimal.ZERO);

        BigDecimal newBalance = request.getType() == TransactionType.INCOME
                ? lastBalance.add(request.getAmount())
                : lastBalance.subtract(request.getAmount());

        transaction.setBalanceAfterTransaction(newBalance);

        // 3. Lưu vào Database
        CashbookTransaction saved = cashbookRepository.save(transaction);
        CashbookTransactionResponse response = mapToResponse(saved);

        // 4. Đẩy sang Google Sheets ngay lập tức
        syncToGoogleSheet(response);

        return response;
    }

    // --- LOGIC PHỤ: TRẢ NỢ NCC ---
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

        // Gọi lại createTransaction -> Tự động lưu DB và sync Sheet
        CashbookTransactionResponse savedTx = createTransaction(txReq);

        // Cập nhật công nợ NCC
        supplier.setDebt(supplier.getDebt() - request.getAmount().doubleValue());
        supplierRepository.save(supplier);

        return savedTx;
    }

    // --- HELPER: ĐỒNG BỘ GOOGLE SHEET ---
    private void syncToGoogleSheet(CashbookTransactionResponse res) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

            // Chuẩn bị hàng dữ liệu (Map đúng theo thứ tự cột trên Sheet của bạn)
            List<Object> rowData = Arrays.asList(
                    res.getCode(),                          // Cột A: Mã
                    res.getTransactionDate().format(formatter), // Cột B: Ngày giờ
                    res.getType().toString(),               // Cột C: Loại
                    res.getCategory(),                      // Cột D: Hạng mục
                    res.getAmount().doubleValue(),          // Cột E: Số tiền (để dạng số để Sheet tính toán được)
                    res.getBalanceAfterTransaction().doubleValue(), // Cột F: Tồn quỹ
                    res.getReferenceName(),                 // Cột G: Đối tượng
                    res.getStoreName(),                     // Cột H: Chi nhánh
                    res.getCreatorName(),                   // Cột I: Người lập
                    res.getDescription()                    // Cột J: Ghi chú
            );

            // Ghi vào tab tên là "Sổ Quỹ" (Thay đổi tùy tên tab của bạn)
            googleSheetService.appendRowToSheet("Transactions", rowData);

        } catch (Exception e) {
            // Log lỗi nhưng không chặn luồng chính (tránh lỗi Sheet làm hỏng giao dịch DB)
            System.err.println("Google Sheet Sync Error: " + e.getMessage());
        }
    }

    // --- CÁC HÀM BỔ TRỢ KHÁC ---
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