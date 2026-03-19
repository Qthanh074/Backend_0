package org.example.backend9.service.core;

import org.example.backend9.dto.request.core.SupplierRequest;
import org.example.backend9.dto.response.core.SupplierResponse;
import org.example.backend9.entity.core.Supplier;
import org.example.backend9.enums.EntityStatus;
import org.example.backend9.repository.core.SupplierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SupplierServiceTest {

    @Mock
    private SupplierRepository supplierRepository;

    @InjectMocks
    private SupplierService supplierService;

    private Supplier mockSupplier;
    private SupplierRequest mockRequest;

    @BeforeEach
    void setUp() {
        // Chuẩn bị dữ liệu mẫu cho Entity
        mockSupplier = new Supplier();
        mockSupplier.setId(1);
        mockSupplier.setCode("SUP001");
        mockSupplier.setName("Nhà cung cấp Vina");
        mockSupplier.setPhone("0987654321");
        mockSupplier.setStatus(EntityStatus.ACTIVE);

        // Chuẩn bị dữ liệu mẫu cho Request
        mockRequest = new SupplierRequest();
        mockRequest.setName("SUP001");
        mockRequest.setName("Nhà cung cấp Vina");
        mockRequest.setPhone("0987654321");
    }

    @Test
    void getAllSuppliers() {
        // Giả lập database trả về danh sách có 1 nhà cung cấp
        when(supplierRepository.findAll()).thenReturn(List.of(mockSupplier));

        // Thực thi hàm cần test
        List<SupplierResponse> result = supplierService.getAllSuppliers();

        // Kiểm tra kết quả
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("Nhà cung cấp Vina", result.get(0).getName());
        verify(supplierRepository, times(1)).findAll();
    }

    @Test
    void createSupplier() {
        // Giả lập khi gọi lệnh save thì trả về mockSupplier
        when(supplierRepository.save(any(Supplier.class))).thenReturn(mockSupplier);

        // Thực thi
        SupplierResponse result = supplierService.createSupplier(mockRequest);

        // Kiểm tra
        assertNotNull(result);
        assertEquals("SUP001", result.getCode());
        assertEquals("Nhà cung cấp Vina", result.getName());
        verify(supplierRepository, times(1)).save(any(Supplier.class));
    }

    @Test
    void updateSupplier() {
        // Giả lập tìm thấy nhà cung cấp (dùng any() để tránh lỗi lệch kiểu Integer)
        when(supplierRepository.findById(any())).thenReturn(Optional.of(mockSupplier));
        when(supplierRepository.save(any(Supplier.class))).thenReturn(mockSupplier);

        // Sửa đổi request để test update
        mockRequest.setName("Nhà cung cấp Vina - Updated");

        // Thực thi
        SupplierResponse result = supplierService.updateSupplier(1, mockRequest);

        // Kiểm tra
        assertNotNull(result);
        verify(supplierRepository, times(1)).findById(any());
        verify(supplierRepository, times(1)).save(any(Supplier.class));
    }

    @Test
    void deleteSupplier() {
        // Giả lập tìm thấy nhà cung cấp trước khi xóa mềm
        when(supplierRepository.findById(any())).thenReturn(Optional.of(mockSupplier));

        // Thực thi
        supplierService.deleteSupplier(1);

        // Kiểm tra xem trạng thái đã được đổi thành INACTIVE chưa
        assertEquals(EntityStatus.INACTIVE, mockSupplier.getStatus());

        // Đảm bảo lệnh save đã được gọi để cập nhật thay đổi xuống DB
        verify(supplierRepository, times(1)).save(mockSupplier);
    }
}