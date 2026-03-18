package org.example.backend9.controller.finance;

import jakarta.validation.Valid;
import org.example.backend9.dto.request.finance.CashbookTransactionRequest;
import org.example.backend9.dto.request.finance.SupplierDebtPaymentRequest;
import org.example.backend9.dto.response.ApiResponse;
import org.example.backend9.dto.response.finance.CashbookTransactionResponse;
import org.example.backend9.enums.PaymentMethod;
import org.example.backend9.enums.TransactionType;
import org.example.backend9.service.finance.CashbookTransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/finance/cashbooks")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN' )")
public class CashbookTransactionController {

    private final CashbookTransactionService cashbookService;

    public CashbookTransactionController(CashbookTransactionService cashbookService) {
        this.cashbookService = cashbookService;
    }

    // API ĐA NĂNG: Cấp data cho cả 4 Trang (Sổ Ngân Hàng, Sổ Tiền Mặt, Ds Phiếu Thu, Ds Phiếu Chi)
    @GetMapping
    public ResponseEntity<ApiResponse<List<CashbookTransactionResponse>>> getTransactions(
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) PaymentMethod method,
            @RequestParam(required = false) String search) {

        List<CashbookTransactionResponse> data = cashbookService.getTransactions(type, method, search);
        return ResponseEntity.ok(new ApiResponse<>(true, "Lấy dữ liệu thành công", data));
    }

    // Lập Phiếu Thu / Phiếu Chi thông thường
    @PostMapping
    public ResponseEntity<ApiResponse<CashbookTransactionResponse>> createTransaction(@Valid @RequestBody CashbookTransactionRequest request) {
        String msg = request.getType() == TransactionType.INCOME ? "Lập phiếu thu thành công" : "Lập phiếu chi thành công";
        return ResponseEntity.ok(new ApiResponse<>(true, msg, cashbookService.createTransaction(request)));
    }

    // API CHUYÊN DỤNG CHO TRANG CÔNG NỢ NCC
    @PostMapping("/pay-supplier")
    public ResponseEntity<ApiResponse<CashbookTransactionResponse>> paySupplierDebt(@Valid @RequestBody SupplierDebtPaymentRequest request) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Thanh toán công nợ thành công", cashbookService.paySupplierDebt(request)));
    }
    @GetMapping("/balance")
// Đổi Double thành BigDecimal ở đây 👇
    public ResponseEntity<ApiResponse<BigDecimal>> getCurrentBalance(@RequestParam PaymentMethod method) {
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Lấy số dư thành công",
                cashbookService.getCurrentBalance(method) // Hàm này trong Service trả về BigDecimal
        ));
    }
    // 2. API lấy danh sách phiếu chi (Chuyên dụng cho trang PaymentPage)
    @GetMapping("/expenses")
    public ResponseEntity<ApiResponse<List<CashbookTransactionResponse>>> getExpenses(
            @RequestParam(required = false) PaymentMethod method) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Lấy danh sách phiếu chi thành công",
                cashbookService.getTransactions(TransactionType.EXPENSE, method, null)));
    }

    // 3. API lấy danh sách phiếu thu (Chuyên dụng cho trang ReceiptPage)
    @GetMapping("/incomes")
    public ResponseEntity<ApiResponse<List<CashbookTransactionResponse>>> getIncomes(
            @RequestParam(required = false) PaymentMethod method) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Lấy danh sách phiếu thu thành công",
                cashbookService.getTransactions(TransactionType.INCOME, method, null)));
    }
}