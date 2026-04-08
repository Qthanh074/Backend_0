package org.example.backend9.repository.core;

import org.example.backend9.entity.core.SupplierProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SupplierProductRepository extends JpaRepository<SupplierProduct, Integer> {

    List<SupplierProduct> findBySupplierId(Integer supplierId);

    // Bổ sung @Modifying và @Query để sửa lỗi khi xóa
    @Modifying
    @Query("DELETE FROM SupplierProduct sp WHERE sp.supplier.id = :supplierId")
    void deleteBySupplierId(@Param("supplierId") Integer supplierId);
}