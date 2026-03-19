package org.example.backend9.service.inventory;

import org.example.backend9.dto.request.inventory.ReturnTicketRequest;
import org.example.backend9.dto.response.inventory.ReturnTicketResponse;
import org.example.backend9.entity.core.Employee;
import org.example.backend9.entity.core.Supplier;
import org.example.backend9.entity.inventory.ProductVariant;
import org.example.backend9.entity.inventory.ReturnTicket;
import org.example.backend9.entity.inventory.ReturnTicketDetail;
import org.example.backend9.enums.TicketStatus;
import org.example.backend9.repository.core.EmployeeRepository;
import org.example.backend9.repository.core.SupplierRepository;
import org.example.backend9.repository.inventory.ProductVariantRepository;
import org.example.backend9.repository.inventory.ReturnTicketDetailRepository;
import org.example.backend9.repository.inventory.ReturnTicketRepository;
import org.example.backend9.repository.sales.CustomerRepository;
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
class ReturnTicketServiceTest {

    @Mock private ReturnTicketRepository ticketRepository;
    @Mock private ReturnTicketDetailRepository detailRepository;
    @Mock private ProductVariantRepository variantRepository;
    @Mock private CustomerRepository customerRepository;
    @Mock private SupplierRepository supplierRepository;
    @Mock private EmployeeRepository employeeRepository;

    @InjectMocks
    private ReturnTicketService returnTicketService;

    private ProductVariant mockVariant;
    private Supplier mockSupplier;

    @BeforeEach
    void setUp() {
        mockVariant = new ProductVariant();
        mockVariant.setId(100);
        mockVariant.setQuantity(10);
        mockVariant.setSku("SKU-RETURN");
        mockVariant.setVariantName("Sản phẩm trả");

        mockSupplier = new Supplier();
        mockSupplier.setId(1);
        mockSupplier.setName("NCC A");
        mockSupplier.setDebt(500000.0);
    }

    @Test
    @DisplayName("1. Create: Khách trả hàng (CUSTOMER_RETURN) - Tồn kho phải tăng")
    void create_CustomerReturn_Success() {
        ReturnTicketRequest req = new ReturnTicketRequest();
        req.setReturnType("CUSTOMER_RETURN");

        ReturnTicketRequest.ReturnDetailRequest dReq = new ReturnTicketRequest.ReturnDetailRequest();
        dReq.setProductVariantId(100L);
        dReq.setReturnQuantity(5); // Trả lại 5 cái
        dReq.setReturnPrice(new BigDecimal("1000"));
        req.setDetails(List.of(dReq));

        when(variantRepository.findById(100L)).thenReturn(Optional.of(mockVariant));
        when(ticketRepository.save(any())).thenAnswer(i -> {
            ReturnTicket t = i.getArgument(0);
            t.setId(1);
            return t;
        });

        ReturnTicketResponse res = returnTicketService.create(req);

        assertEquals(15, mockVariant.getQuantity()); // 10 + 5 = 15
        verify(variantRepository).save(mockVariant);
    }

    @Test
    @DisplayName("2. Create: Trả hàng NCC (SUPPLIER_RETURN) - Tồn kho giảm, Nợ NCC giảm")
    void create_SupplierReturn_Success() {
        ReturnTicketRequest req = new ReturnTicketRequest();
        req.setReturnType("SUPPLIER_RETURN");
        req.setSupplierId(1);

        ReturnTicketRequest.ReturnDetailRequest dReq = new ReturnTicketRequest.ReturnDetailRequest();
        dReq.setProductVariantId(100L);
        dReq.setReturnQuantity(2); // Trả NCC 2 cái
        dReq.setReturnPrice(new BigDecimal("100000")); // Tổng 200k
        req.setDetails(List.of(dReq));

        when(supplierRepository.findById(1)).thenReturn(Optional.of(mockSupplier));
        when(variantRepository.findById(100L)).thenReturn(Optional.of(mockVariant));
        when(ticketRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        returnTicketService.create(req);

        assertEquals(8, mockVariant.getQuantity()); // 10 - 2 = 8
        assertEquals(300000.0, mockSupplier.getDebt()); // 500k - 200k = 300k
        verify(supplierRepository).save(mockSupplier);
    }

    @Test
    @DisplayName("3. Delete: Hoàn tác kho & tài chính khi xóa phiếu trả NCC")
    void delete_SupplierReturn_UndoSuccess() {
        ReturnTicket ticket = new ReturnTicket();
        ticket.setId(1);
        ticket.setReturnType("SUPPLIER_RETURN");
        ticket.setSupplier(mockSupplier);
        ticket.setTotalRefundAmount(new BigDecimal("100000"));

        ReturnTicketDetail detail = new ReturnTicketDetail();
        detail.setProductVariant(mockVariant);
        detail.setReturnQuantity(3);

        when(ticketRepository.findById(1)).thenReturn(Optional.of(ticket));
        when(detailRepository.findByReturnTicketId(1)).thenReturn(List.of(detail));

        returnTicketService.delete(1);

        // Hoàn tác trả NCC: Kho tăng lại, Nợ tăng lại
        assertEquals(13, mockVariant.getQuantity()); // 10 + 3
        assertEquals(600000.0, mockSupplier.getDebt()); // 500k + 100k
        verify(ticketRepository).delete(ticket);
    }

    @Test
    @DisplayName("4. Update: Lỗi khi kho không đủ để trả hàng cho NCC")
    void create_SupplierReturn_Fail_OutOfStock() {
        ReturnTicketRequest req = new ReturnTicketRequest();
        req.setReturnType("SUPPLIER_RETURN");

        ReturnTicketRequest.ReturnDetailRequest dReq = new ReturnTicketRequest.ReturnDetailRequest();
        dReq.setProductVariantId(100L);
        dReq.setReturnQuantity(50); // Kho có 10 mà đòi trả 50
        req.setDetails(List.of(dReq));

        when(variantRepository.findById(100L)).thenReturn(Optional.of(mockVariant));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> returnTicketService.create(req));
        assertTrue(ex.getMessage().contains("Kho không đủ hàng"));
    }
}