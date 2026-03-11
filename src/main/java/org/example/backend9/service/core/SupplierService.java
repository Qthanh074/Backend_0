package org.example.backend9.service.core;

import org.example.backend9.dto.request.core.SupplierRequest;
import org.example.backend9.dto.response.core.SupplierResponse;
import org.example.backend9.entity.core.Supplier;
import org.example.backend9.enums.EntityStatus;
import org.example.backend9.repository.core.SupplierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SupplierService {
    private final SupplierRepository supplierRepository;

    public SupplierService(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    private SupplierResponse mapToResponse(Supplier supplier) {
        SupplierResponse res = new SupplierResponse();
        res.setId(supplier.getId());
        res.setCode(supplier.getCode());
        res.setName(supplier.getName());
        res.setContactPerson(supplier.getContactPerson());
        res.setPhone(supplier.getPhone());
        res.setEmail(supplier.getEmail());
        res.setAddress(supplier.getAddress());
        res.setDebt(supplier.getDebt());
        res.setStatus(supplier.getStatus());
        return res;
    }

    public List<SupplierResponse> getAllSuppliers() {
        return supplierRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public SupplierResponse createSupplier(SupplierRequest request) {
        Supplier supplier = new Supplier();
        supplier.setCode("NCC" + System.currentTimeMillis() % 10000);
        supplier.setName(request.getName());
        supplier.setContactPerson(request.getContactPerson());
        supplier.setPhone(request.getPhone());
        supplier.setEmail(request.getEmail());
        supplier.setAddress(request.getAddress());
        supplier.setDebt(0.0);
        supplier.setStatus(EntityStatus.ACTIVE);
        return mapToResponse(supplierRepository.save(supplier));
    }

    @Transactional
    public SupplierResponse updateSupplier(Integer id, SupplierRequest request) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhà cung cấp"));
        supplier.setName(request.getName());
        supplier.setContactPerson(request.getContactPerson());
        supplier.setPhone(request.getPhone());
        supplier.setEmail(request.getEmail());
        supplier.setAddress(request.getAddress());
        return mapToResponse(supplierRepository.save(supplier));
    }

    @Transactional
    public void deleteSupplier(Integer id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhà cung cấp"));
        supplier.setStatus(EntityStatus.INACTIVE);
        supplierRepository.save(supplier);
    }
}