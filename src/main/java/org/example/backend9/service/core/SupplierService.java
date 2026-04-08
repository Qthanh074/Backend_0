package org.example.backend9.service.core;

import org.example.backend9.dto.request.core.SupplierRequest;
import org.example.backend9.dto.response.core.SupplierResponse;
import org.example.backend9.entity.core.Supplier;
import org.example.backend9.enums.EntityStatus;
import org.example.backend9.repository.core.SupplierRepository;
import org.example.backend9.repository.core.SupplierProductRepository;
import org.example.backend9.repository.inventory.ProductVariantRepository;
import org.example.backend9.entity.core.SupplierProduct;
import org.example.backend9.entity.inventory.ProductVariant;
import org.example.backend9.dto.request.core.SupplierProductRequest;
import org.example.backend9.dto.response.core.SupplierProductResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SupplierService {
    private final SupplierRepository supplierRepository;
    private final SupplierProductRepository supplierProductRepository;
    private final ProductVariantRepository productVariantRepository;

    public SupplierService(SupplierRepository supplierRepository,
                           SupplierProductRepository supplierProductRepository,
                           ProductVariantRepository productVariantRepository) {
        this.supplierRepository = supplierRepository;
        this.supplierProductRepository = supplierProductRepository;
        this.productVariantRepository = productVariantRepository;
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

    public List<SupplierProductResponse> getProductsBySupplier(Integer supplierId) {
        return supplierProductRepository.findBySupplierId(supplierId).stream().map(sp -> {
            ProductVariant v = sp.getProductVariant();
            String cName = v.getColor() != null ? v.getColor().getName() : "";
            String sName = v.getSize() != null ? v.getSize().getName() : "";
            String attr = (cName + " " + sName).trim();
            String pName = v.getProduct().getName();
            String vName = attr.isEmpty() ? pName : pName + " (" + attr.replace(" ", " - ") + ")";

            return SupplierProductResponse.builder()
                    .variantId(v.getId())
                    .sku(v.getSku())
                    .variantName(vName)
                    .costPrice(sp.getImportPrice())
                    .quantity(v.getQuantity())
                    .build();
        }).collect(Collectors.toList());
    }

    @Transactional
    public void configSupplierProducts(Integer supplierId, List<SupplierProductRequest> requests) {
        Supplier supplier = supplierRepository.findById(supplierId).orElseThrow();
        supplierProductRepository.deleteBySupplierId(supplierId); // Xóa cấu hình cũ

        List<SupplierProduct> newConfigs = requests.stream().map(req -> {
            ProductVariant v = productVariantRepository.findById(req.getVariantId().longValue()).orElseThrow();
            SupplierProduct sp = new SupplierProduct();
            sp.setSupplier(supplier);
            sp.setProductVariant(v);
            sp.setImportPrice(req.getImportPrice());
            return sp;
        }).collect(Collectors.toList());

        supplierProductRepository.saveAll(newConfigs);
    }
}