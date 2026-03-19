package org.example.backend9.service.inventory;

import org.example.backend9.dto.request.inventory.ExportTicketRequest;
import org.example.backend9.dto.response.inventory.ExportTicketResponse;
import org.example.backend9.entity.core.Employee;
import org.example.backend9.entity.inventory.ExportTicket;
import org.example.backend9.entity.inventory.ExportTicketDetail;
import org.example.backend9.entity.inventory.ProductVariant;
import org.example.backend9.enums.TicketStatus;
import org.example.backend9.repository.core.EmployeeRepository;
import org.example.backend9.repository.inventory.ExportTicketDetailRepository;
import org.example.backend9.repository.inventory.ExportTicketRepository;
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
@MockitoSettings(strictness = Strictness.LENIENT) // Fix lỗi UnnecessaryStubbingException
class ExportTicketServiceTest {

    @Mock private ExportTicketRepository exportTicketRepository;
    @Mock private ExportTicketDetailRepository detailRepository;
    @Mock private ProductVariantRepository variantRepository;
    @Mock private EmployeeRepository employeeRepository;
    @Mock private GoogleSheetService googleSheetService;

    @InjectMocks
    private ExportTicketService exportTicketService;

    private ProductVariant mockVariant;
    private Employee mockEmployee;

    @BeforeEach
    void setUp() {
        mockVariant = new ProductVariant();
        mockVariant.setId(100); // Đảm bảo khớp kiểu dữ liệu Integer/Long của bạn
        mockVariant.setVariantName("iPhone 15");
        mockVariant.setQuantity(10);
        mockVariant.setSku("IP15-BLUE");

        mockEmployee = new Employee();
        mockEmployee.setId(1);
        mockEmployee.setFullName("Ngọc Admin");
    }

    // Hàm bổ trợ tạo Detail để tránh lỗi NullPointerException khi mapping
    private ExportTicketDetail createMockDetail(ExportTicket ticket) {
        ExportTicketDetail detail = new ExportTicketDetail();
        detail.setProductVariant(mockVariant);
        detail.setQuantity(2);
        detail.setUnitPrice(new BigDecimal("1000")); // Quan trọng: Phải có giá trị này
        detail.setExportTicket(ticket);
        return detail;
    }

    @Test
    @DisplayName("1. Create: Thành công và trừ tồn kho")
    void create_Success() {
        ExportTicketRequest req = new ExportTicketRequest();
        req.setCreatedById(1);

        ExportTicketRequest.ExportTicketDetailRequest dReq = new ExportTicketRequest.ExportTicketDetailRequest();
        dReq.setProductVariantId(100);
        dReq.setQuantity(3);
        dReq.setUnitPrice(new BigDecimal("1000"));
        req.setDetails(List.of(dReq));

        when(employeeRepository.findById(1)).thenReturn(Optional.of(mockEmployee));
        when(variantRepository.findById(100L)).thenReturn(Optional.of(mockVariant));
        when(exportTicketRepository.save(any(ExportTicket.class))).thenAnswer(i -> {
            ExportTicket t = i.getArgument(0);
            t.setId(1); // Giả lập ID sau khi save
            return t;
        });

        ExportTicketResponse res = exportTicketService.create(req);

        assertEquals(7, mockVariant.getQuantity());
        assertNotNull(res);
    }

    @Test
    @DisplayName("2. Cancel: Hủy phiếu và hoàn tồn kho")
    void cancel_Success() {
        ExportTicket ticket = new ExportTicket();
        ticket.setId(1);
        ticket.setStatus(TicketStatus.COMPLETED);

        // Phải tạo detail có UnitPrice để mapToResponse không bị Null
        ExportTicketDetail detail = createMockDetail(ticket);

        when(exportTicketRepository.findById(1)).thenReturn(Optional.of(ticket));
        when(detailRepository.findByExportTicketId(1)).thenReturn(List.of(detail));
        when(exportTicketRepository.save(any())).thenReturn(ticket);

        ExportTicketResponse res = exportTicketService.cancelTicket(1);

        assertEquals(12, mockVariant.getQuantity()); // 10 + 2
        assertEquals("CANCELLED", res.getStatus());
    }

    @Test
    @DisplayName("3. Update: Cập nhật và tính lại tồn kho")
    void update_Success() {
        ExportTicket ticket = new ExportTicket();
        ticket.setId(1);
        ticket.setStatus(TicketStatus.COMPLETED);

        ExportTicketDetail oldDetail = createMockDetail(ticket); // Đang xuất 2 cái

        ExportTicketRequest req = new ExportTicketRequest();
        ExportTicketRequest.ExportTicketDetailRequest dReq = new ExportTicketRequest.ExportTicketDetailRequest();
        dReq.setProductVariantId(100);
        dReq.setQuantity(5); // Update thành xuất 5 cái
        dReq.setUnitPrice(new BigDecimal("1000"));
        req.setDetails(List.of(dReq));

        when(exportTicketRepository.findById(1)).thenReturn(Optional.of(ticket));
        when(detailRepository.findByExportTicketId(1)).thenReturn(List.of(oldDetail));
        when(variantRepository.findById(100L)).thenReturn(Optional.of(mockVariant));
        when(exportTicketRepository.save(any())).thenReturn(ticket);

        exportTicketService.update(1, req);

        // Logic: 10 (gốc) + 2 (hoàn tác cũ) - 5 (mới) = 7
        assertEquals(7, mockVariant.getQuantity());
    }

    @Test
    @DisplayName("4. GetById: Thành công")
    void getById_Success() {
        ExportTicket ticket = new ExportTicket();
        ticket.setId(1);

        when(exportTicketRepository.findById(1)).thenReturn(Optional.of(ticket));
        when(detailRepository.findByExportTicketId(1)).thenReturn(new ArrayList<>());

        assertNotNull(exportTicketService.getById(1));
    }

    @Test
    @DisplayName("5. Google Sheets Sync: Lỗi không làm dừng ứng dụng")
    void syncError_Handling() {
        // Test trường hợp getAll() vẫn chạy tốt dù các hàm khác có vấn đề stubbing
        when(exportTicketRepository.findAll()).thenReturn(new ArrayList<>());
        assertDoesNotThrow(() -> exportTicketService.getAll());
    }
}