package org.example.backend9.service.core;

import org.example.backend9.dto.request.core.SupplierRequest;
import org.example.backend9.dto.response.core.SupplierResponse;
import org.example.backend9.entity.core.Supplier;
import org.example.backend9.enums.EntityStatus;
import org.example.backend9.repository.core.SupplierRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SupplierServiceIntegrationTest {

    @Autowired
    private SupplierService supplierService;

    @Autowired
    private SupplierRepository supplierRepository;

    @MockBean
    private org.example.backend9.service.GoogleSheetService googleSheetService;

    @Test
    void createSupplier_Success_ShouldGenerateCodeAndSetDebtZero() {
        SupplierRequest request = new SupplierRequest();
        request.setName("Công ty ABC");
        request.setContactPerson("Mr. A");
        request.setPhone("0123");
        request.setEmail("abc@abc.com");

        SupplierResponse response = supplierService.createSupplier(request);

        assertNotNull(response.getId());
        assertTrue(response.getCode().startsWith("NCC"));
        assertEquals("Công ty ABC", response.getName());
        assertEquals(0.0, response.getDebt(), "Công nợ ban đầu phải bằng 0");
        assertEquals(EntityStatus.ACTIVE, response.getStatus());
    }

    @Test
    void getAllSuppliers_ShouldReturnList() {
        Supplier supplier = new Supplier();
        supplier.setCode("NCC_01");
        supplier.setName("Nhà cung cấp 1");
        supplier.setStatus(EntityStatus.ACTIVE);
        supplierRepository.save(supplier);

        List<SupplierResponse> list = supplierService.getAllSuppliers();
        assertFalse(list.isEmpty());
        assertTrue(list.stream().anyMatch(s -> s.getCode().equals("NCC_01")));
    }

    @Test
    void updateSupplier_Success_ShouldUpdateFields() {
        Supplier supplier = new Supplier();
        supplier.setCode("NCC_OLD");
        supplier.setName("Tên cũ");
        supplier.setStatus(EntityStatus.ACTIVE);
        supplier = supplierRepository.save(supplier);

        SupplierRequest request = new SupplierRequest();
        request.setName("Tên mới");
        request.setContactPerson("Ms. B");

        SupplierResponse response = supplierService.updateSupplier(supplier.getId(), request);

        assertEquals("Tên mới", response.getName());
        assertEquals("Ms. B", response.getContactPerson());
    }

    @Test
    void deleteSupplier_ShouldSetStatusToInactive() {
        Supplier supplier = new Supplier();
        supplier.setCode("NCC_DEL");
        supplier.setName("NCC sắp xóa");
        supplier.setStatus(EntityStatus.ACTIVE);
        supplier = supplierRepository.save(supplier);

        supplierService.deleteSupplier(supplier.getId());

        Supplier deletedSupplier = supplierRepository.findById(supplier.getId()).orElseThrow();
        assertEquals(EntityStatus.INACTIVE, deletedSupplier.getStatus());
    }
}