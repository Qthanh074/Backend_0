package org.example.backend9.service.inventory;

import org.example.backend9.dto.request.inventory.TransferTicketRequest;
import org.example.backend9.dto.response.inventory.TransferTicketResponse;
import org.example.backend9.entity.core.Store;
import org.example.backend9.entity.inventory.ProductVariant;
import org.example.backend9.entity.inventory.TransferTicket;
import org.example.backend9.entity.inventory.TransferTicketDetail;
import org.example.backend9.enums.TicketStatus;
import org.example.backend9.repository.core.EmployeeRepository;
import org.example.backend9.repository.core.StoreRepository;
import org.example.backend9.repository.inventory.ProductVariantRepository;
import org.example.backend9.repository.inventory.TransferTicketDetailRepository;
import org.example.backend9.repository.inventory.TransferTicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TransferTicketServiceTest {

    @Mock private TransferTicketRepository ticketRepository;
    @Mock private TransferTicketDetailRepository detailRepository;
    @Mock private ProductVariantRepository variantRepository;
    @Mock private StoreRepository storeRepository;
    @Mock private EmployeeRepository employeeRepository;

    @InjectMocks
    private TransferTicketService transferTicketService;

    private Store fromStore;
    private Store toStore;
    private ProductVariant mockVariant;
    private TransferTicket mockTicket;

    @BeforeEach
    void setUp() {
        fromStore = new Store(); fromStore.setName("Kho Xuất");
        toStore = new Store(); toStore.setName("Kho Nhập");

        mockVariant = new ProductVariant();
        mockVariant.setId(100);
        mockVariant.setQuantity(50);
        mockVariant.setSku("SKU-TRANSFER");

        mockTicket = new TransferTicket();
        mockTicket.setId(1);
        mockTicket.setStatus(TicketStatus.PENDING);
        mockTicket.setFromStore(fromStore);
        mockTicket.setToStore(toStore);
    }

    @Test
    @DisplayName("1. Create: Tạo phiếu chuyển kho thành công")
    void create_Success() {
        TransferTicketRequest req = new TransferTicketRequest();
        req.setFromStoreId(1);
        req.setToStoreId(2);

        TransferTicketRequest.TransferTicketDetailRequest dReq = new TransferTicketRequest.TransferTicketDetailRequest();
        dReq.setProductVariantId(100L);
        dReq.setQuantity(10);
        req.setDetails(List.of(dReq));

        when(storeRepository.findById(1)).thenReturn(Optional.of(fromStore));
        when(storeRepository.findById(2)).thenReturn(Optional.of(toStore));
        when(variantRepository.findById(100L)).thenReturn(Optional.of(mockVariant));
        when(ticketRepository.save(any())).thenReturn(mockTicket);

        TransferTicketResponse res = transferTicketService.create(req);

        assertNotNull(res);
        assertEquals(TicketStatus.PENDING.name(), res.getStatus());
        verify(detailRepository).saveAll(any());
    }

    @Test
    @DisplayName("2. Process: Xuất kho - Trừ số lượng ở kho xuất")
    void processTransfer_Success() {
        TransferTicketDetail detail = new TransferTicketDetail();
        detail.setProductVariant(mockVariant);
        detail.setQuantity(20);

        when(ticketRepository.findById(1)).thenReturn(Optional.of(mockTicket));
        when(detailRepository.findByTransferTicketId(1)).thenReturn(List.of(detail));
        when(ticketRepository.save(any())).thenReturn(mockTicket);

        transferTicketService.processTransfer(1);

        assertEquals(30, mockVariant.getQuantity()); // 50 - 20 = 30
        assertEquals(TicketStatus.IN_TRANSIT, mockTicket.getStatus());
        verify(variantRepository).save(mockVariant);
    }

    @Test
    @DisplayName("3. Confirm: Xác nhận nhận hàng - Cộng số lượng vào kho nhập")
    void confirmReceipt_Success() {
        mockTicket.setStatus(TicketStatus.IN_TRANSIT);
        TransferTicketDetail detail = new TransferTicketDetail();
        detail.setProductVariant(mockVariant);
        detail.setQuantity(20);

        when(ticketRepository.findById(1)).thenReturn(Optional.of(mockTicket));
        when(detailRepository.findByTransferTicketId(1)).thenReturn(List.of(detail));
        when(ticketRepository.save(any())).thenReturn(mockTicket);

        transferTicketService.confirmReceipt(1);

        assertEquals(70, mockVariant.getQuantity()); // 50 + 20 = 70 (Giả lập cộng vào kho đích)
        assertEquals(TicketStatus.COMPLETED, mockTicket.getStatus());
    }

    @Test
    @DisplayName("4. Update/Delete: Không được thao tác khi hàng đang đi đường")
    void update_Fail_WhenInTransit() {
        mockTicket.setStatus(TicketStatus.IN_TRANSIT);
        when(ticketRepository.findById(1)).thenReturn(Optional.of(mockTicket));

        assertThrows(RuntimeException.class, () -> transferTicketService.update(1, new TransferTicketRequest()));
        assertThrows(RuntimeException.class, () -> transferTicketService.delete(1));
    }

    @Test
    @DisplayName("5. Process: Thất bại nếu kho xuất không đủ hàng")
    void processTransfer_Fail_OutOfStock() {
        TransferTicketDetail detail = new TransferTicketDetail();
        detail.setProductVariant(mockVariant);
        detail.setQuantity(100); // Kho có 50, đòi chuyển 100

        when(ticketRepository.findById(1)).thenReturn(Optional.of(mockTicket));
        when(detailRepository.findByTransferTicketId(1)).thenReturn(List.of(detail));

        assertThrows(RuntimeException.class, () -> transferTicketService.processTransfer(1));
    }
}