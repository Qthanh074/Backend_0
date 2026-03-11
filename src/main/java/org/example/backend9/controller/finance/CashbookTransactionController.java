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

import java.util.List;

@RestController
@RequestMapping("/api/finance/cashbooks")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'ACCOUNTANT')")
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
}