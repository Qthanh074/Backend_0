package org.example.backend9.service.inventory;

import org.example.backend9.dto.request.inventory.ImportTicketRequest;
import org.example.backend9.dto.response.inventory.ImportTicketResponse;
import org.example.backend9.entity.core.Employee;
import org.example.backend9.entity.core.Supplier;
import org.example.backend9.entity.inventory.ImportTicket;
import org.example.backend9.entity.inventory.ImportTicketDetail;
import org.example.backend9.entity.inventory.ProductVariant;
import org.example.backend9.enums.TicketStatus;
import org.example.backend9.repository.core.EmployeeRepository;
import org.example.backend9.repository.core.SupplierRepository;
import org.example.backend9.repository.inventory.ImportTicketDetailRepository;
import org.example.backend9.repository.inventory.ImportTicketRepository;
import org.example.backend9.repository.inventory.ProductVariantRepository;
import org.example.backend9.service.GoogleSheetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ImportTicketServiceTest {

    @Mock private ImportTicketRepository importTicketRepository;
    @Mock private ImportTicketDetailRepository detailRepository;
    @Mock private ProductVariantRepository variantRepository;
    @Mock private SupplierRepository supplierRepository;
    @Mock private EmployeeRepository employeeRepository;
    @Mock private GoogleSheetService googleSheetService;

    @InjectMocks
    private ImportTicketService importTicketService;

    private Supplier mockSupplier;
    private ProductVariant mockVariant;
    private Employee mockEmployee;

    @BeforeEach
    void setUp() {
        mockSupplier = new Supplier();
        mockSupplier.setId(1);
        mockSupplier.setName("Nhà cung cấp A");
        mockSupplier.setDebt(0.0);

        mockVariant = new ProductVariant();
        mockVariant.setId(100); // Kiểu Integer hoặc Long tùy Entity của bạn
        mockVariant.setQuantity(10);
        mockVariant.setVariantName("Sản phẩm Test");
        mockVariant.setSku("SKU-001");

        mockEmployee = new Employee();
        mockEmployee.setId(1);
        mockEmployee.setFullName("Ngọc Admin");
    }

    // Hàm bổ trợ tạo Detail giả để tránh NullPointerException khi mapping
    private ImportTicketDetail createMockDetail(ImportTicket ticket) {
        ImportTicketDetail detail = new ImportTicketDetail();
        detail.setProductVariant(mockVariant);
        detail.setQuantity(5);
        detail.setUnitPrice(new BigDecimal("10000"));
        detail.setImportTicket(ticket);
        return detail;
    }

    @Test
    @DisplayName("1. Create: Nhập kho ghi nợ - Tồn kho tăng, Nợ NCC tăng")
    void create_WithDebt_Success() {
        // Given
        ImportTicketRequest req = new ImportTicketRequest();
        req.setSupplierId(1);
        req.setCreatedById(1);
        req.setPaidAmount(new BigDecimal("20000")); // Trả trước 20k

        ImportTicketRequest.ImportTicketDetailRequest dReq = new ImportTicketRequest.ImportTicketDetailRequest();
        dReq.setProductVariantId(100);
        dReq.setQuantity(5); // Nhập 5
        dReq.setUnitPrice(new BigDecimal("10000")); // Tổng 50k -> Nợ 30k
        req.setDetails(List.of(dReq));

        when(supplierRepository.findById(1)).thenReturn(Optional.of(mockSupplier));
        when(employeeRepository.findById(1)).thenReturn(Optional.of(mockEmployee));
        when(variantRepository.findById(100L)).thenReturn(Optional.of(mockVariant));
        when(importTicketRepository.save(any())).thenAnswer(i -> {
            ImportTicket t = i.getArgument(0);
            t.setId(1);
            return t;
        });

        // When
        ImportTicketResponse res = importTicketService.create(req);

        // Then
        assertEquals(15, mockVariant.getQuantity()); // 10 + 5
        assertEquals(30000.0, mockSupplier.getDebt()); // Nợ 30k
        assertEquals("Ghi nợ", res.getStatus());
        verify(supplierRepository, atLeastOnce()).save(mockSupplier);
    }

    @Test
    @DisplayName("2. Update: Thay đổi số lượng nhập - Tính lại tồn kho & công nợ")
    void update_ChangeQuantity_Success() {
        // Given: Phiếu cũ nợ 10k, nhập 5 cái
        ImportTicket ticket = new ImportTicket();
        ticket.setId(1);
        ticket.setSupplier(mockSupplier);
        ticket.setDebtAmount(new BigDecimal("10000"));
        ticket.setStatus(TicketStatus.DEBT);
        mockSupplier.setDebt(10000.0);

        ImportTicketDetail oldDetail = createMockDetail(ticket);

        ImportTicketRequest req = new ImportTicketRequest();
        req.setSupplierId(1);
        req.setPaidAmount(new BigDecimal("50000")); // Trả 50k

        ImportTicketRequest.ImportTicketDetailRequest dReq = new ImportTicketRequest.ImportTicketDetailRequest();
        dReq.setProductVariantId(100);
        dReq.setQuantity(8); // Nhập mới 8 cái
        dReq.setUnitPrice(new BigDecimal("10000")); // Tổng 80k -> Nợ 30k
        req.setDetails(List.of(dReq));

        when(importTicketRepository.findById(1)).thenReturn(Optional.of(ticket));
        when(detailRepository.findByImportTicketId(1)).thenReturn(List.of(oldDetail));
        when(supplierRepository.findById(1)).thenReturn(Optional.of(mockSupplier));
        when(variantRepository.findById(100L)).thenReturn(Optional.of(mockVariant));
        when(importTicketRepository.save(any())).thenReturn(ticket);

        // When
        importTicketService.update(1, req);

        // Then: 10 (gốc) - 5 (hoàn tác cũ) + 8 (mới) = 13
        assertEquals(13, mockVariant.getQuantity());
        // Nợ cũ 10k bị trừ -> 0. Sau đó nợ mới 30k -> 30k
        assertEquals(30000.0, mockSupplier.getDebt());
    }

    @Test
    @DisplayName("3. Cancel: Hủy phiếu - Hoàn tồn kho (trừ đi) & Trừ nợ NCC")
    void cancel_Success() {
        // Given
        ImportTicket ticket = new ImportTicket();
        ticket.setId(1);
        ticket.setSupplier(mockSupplier);
        ticket.setDebtAmount(new BigDecimal("50000"));
        mockSupplier.setDebt(50000.0);

        ImportTicketDetail detail = createMockDetail(ticket); // Nhập 5 cái

        when(importTicketRepository.findById(1)).thenReturn(Optional.of(ticket));
        when(detailRepository.findByImportTicketId(1)).thenReturn(List.of(detail));
        when(importTicketRepository.save(any())).thenReturn(ticket);

        // When
        importTicketService.cancelTicket(1);

        // Then
        assertEquals(5, mockVariant.getQuantity()); // 10 - 5 = 5
        assertEquals(0.0, mockSupplier.getDebt()); // Hết nợ
        verify(importTicketRepository).save(ticket);
    }

    @Test
    @DisplayName("4. GetById: Trả về đúng thông tin")
    void getById_Success() {
        ImportTicket ticket = new ImportTicket();
        ticket.setId(1);
        ticket.setSupplier(mockSupplier);

        when(importTicketRepository.findById(1)).thenReturn(Optional.of(ticket));
        when(detailRepository.findByImportTicketId(1)).thenReturn(new ArrayList<>());

        ImportTicketResponse res = importTicketService.getById(1);
        assertNotNull(res);
        assertEquals("Nhà cung cấp A", res.getSupplierName());
    }
}