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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CashbookTransactionService {

    private final CashbookTransactionRepository cashbookRepository;
    private final StoreRepository storeRepository;
    private final EmployeeRepository employeeRepository;
    private final SupplierRepository supplierRepository; // Tiêm thêm Supplier

    public CashbookTransactionService(CashbookTransactionRepository cashbookRepository,
                                      StoreRepository storeRepository,
                                      EmployeeRepository employeeRepository,
                                      SupplierRepository supplierRepository) {
        this.cashbookRepository = cashbookRepository;
        this.storeRepository = storeRepository;
        this.employeeRepository = employeeRepository;
        this.supplierRepository = supplierRepository;
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

    // PHỤC VỤ 4 TRANG: TIỀN MẶT, NGÂN HÀNG, PHIẾU THU, PHIẾU CHI
    public List<CashbookTransactionResponse> getTransactions(TransactionType type, PaymentMethod method, String search) {
        return cashbookRepository.filterTransactions(type, method, search)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    public CashbookTransactionResponse createTransaction(CashbookTransactionRequest request) {
        Store store = storeRepository.findById(request.getStoreId()).orElseThrow(() -> new RuntimeException("Cửa hàng không tồn tại"));
        Employee creator = employeeRepository.findById(request.getCreatorId()).orElseThrow(() -> new RuntimeException("Nhân viên không tồn tại"));

        CashbookTransaction transaction = new CashbookTransaction();
        transaction.setCode((request.getType() == TransactionType.INCOME ? "PT" : "PC") + System.currentTimeMillis() % 1000000);
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

        // Tính tồn quỹ
        BigDecimal lastBalance = cashbookRepository.findTopByStoreIdOrderByTransactionDateDesc(store.getId())
                .map(CashbookTransaction::getBalanceAfterTransaction).orElse(BigDecimal.ZERO);

        BigDecimal newBalance = request.getType() == TransactionType.INCOME
                ? lastBalance.add(request.getAmount())
                : lastBalance.subtract(request.getAmount());
        transaction.setBalanceAfterTransaction(newBalance);

        return mapToResponse(cashbookRepository.save(transaction));
    }

    // PHỤC VỤ TRANG CÔNG NỢ NCC: Tạo Phiếu Chi + Trừ Nợ cùng 1 lúc
    @Transactional
    public CashbookTransactionResponse paySupplierDebt(SupplierDebtPaymentRequest request) {
        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Nhà cung cấp"));

        // 1. Tạo Phiếu Chi
        CashbookTransactionRequest txReq = new CashbookTransactionRequest();
        txReq.setType(TransactionType.EXPENSE);
        txReq.setMethod(request.getMethod());
        txReq.setCategory("Trả nợ nhà cung cấp");
        txReq.setDescription(request.getNotes() != null ? request.getNotes() : "Thanh toán công nợ cho " + supplier.getName());
        txReq.setReferenceName(supplier.getName());
        txReq.setAmount(request.getAmount());
        txReq.setStoreId(request.getStoreId());
        txReq.setCreatorId(request.getCreatorId());

        CashbookTransactionResponse savedTx = createTransaction(txReq);

        // 2. Trừ Nợ Nhà Cung Cấp
        supplier.setDebt(supplier.getDebt() - request.getAmount().doubleValue());
        supplierRepository.save(supplier);

        return savedTx;
    }
}