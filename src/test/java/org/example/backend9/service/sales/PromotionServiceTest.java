package org.example.backend9.service.sales;

import org.example.backend9.dto.request.sales.PromotionCheckRequest;
import org.example.backend9.dto.request.sales.PromotionRequest;
import org.example.backend9.dto.response.sales.PromotionCheckResponse;
import org.example.backend9.entity.sales.Promotion;
import org.example.backend9.enums.DiscountType;
import org.example.backend9.repository.sales.PromotionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PromotionServiceTest {

    @Mock
    private PromotionRepository promotionRepository;

    @InjectMocks
    private PromotionService promotionService;

    private Promotion mockPromotion;

    @BeforeEach
    void setUp() {
        mockPromotion = new Promotion();
        mockPromotion.setId(1);
        mockPromotion.setCode("SALE2026");
        mockPromotion.setName("Giảm giá mùa xuân");
        mockPromotion.setIsActive(true);
        mockPromotion.setStartDate(LocalDate.now().minusDays(1)); // Đã bắt đầu
        mockPromotion.setEndDate(LocalDate.now().plusDays(5));   // Chưa kết thúc
        mockPromotion.setMinPurchase(new BigDecimal("200000"));  // Tối thiểu 200k
    }

    @Test
    @DisplayName("1. Validate: Thành công với mã giảm giá cố định (FIXED)")
    void validate_FixedDiscount_Success() {
        // Given
        mockPromotion.setDiscountType(DiscountType.FIXED);
        mockPromotion.setDiscountValue(new BigDecimal("50000")); // Giảm 50k

        PromotionCheckRequest req = new PromotionCheckRequest();
        req.setCode("SALE2026");
        req.setOrderTotal(new BigDecimal("300000")); // Đơn 300k (> 200k)

        when(promotionRepository.findByCode("SALE2026")).thenReturn(Optional.of(mockPromotion));

        // When
        PromotionCheckResponse res = promotionService.validatePromotion(req);

        // Then
        assertTrue(res.isValid());
        assertEquals(new BigDecimal("50000"), res.getDiscountAmount());
    }

    @Test
    @DisplayName("2. Validate: Thành công với mã % và có chặn Max Discount")
    void validate_PercentageDiscount_WithMax_Success() {
        // Given: Giảm 10%, tối đa 20k
        mockPromotion.setDiscountType(DiscountType.PERCENTAGE);
        mockPromotion.setDiscountValue(new BigDecimal("10"));
        mockPromotion.setMaxDiscount(new BigDecimal("20000"));

        PromotionCheckRequest req = new PromotionCheckRequest();
        req.setCode("SALE2026");
        req.setOrderTotal(new BigDecimal("500000")); // 10% của 500k là 50k -> phải bị chặn còn 20k

        when(promotionRepository.findByCode("SALE2026")).thenReturn(Optional.of(mockPromotion));

        // When
        PromotionCheckResponse res = promotionService.validatePromotion(req);

        // Then
        assertTrue(res.isValid());
        assertEquals(new BigDecimal("20000"), res.getDiscountAmount());
    }

    @Test
    @DisplayName("3. Validate: Thất bại do chưa đạt giá trị đơn hàng tối thiểu")
    void validate_Fail_MinPurchase() {
        // Given: Đơn 100k < 200k tối thiểu
        PromotionCheckRequest req = new PromotionCheckRequest();
        req.setCode("SALE2026");
        req.setOrderTotal(new BigDecimal("100000"));

        when(promotionRepository.findByCode("SALE2026")).thenReturn(Optional.of(mockPromotion));

        // When
        PromotionCheckResponse res = promotionService.validatePromotion(req);

        // Then
        assertFalse(res.isValid());
        assertTrue(res.getMessage().contains("chưa đạt mức tối thiểu"));
    }

    @Test
    @DisplayName("4. Validate: Thất bại do mã đã hết hạn")
    void validate_Fail_Expired() {
        mockPromotion.setEndDate(LocalDate.now().minusDays(1)); // Hết hạn hôm qua

        PromotionCheckRequest req = new PromotionCheckRequest();
        req.setCode("SALE2026");
        req.setOrderTotal(new BigDecimal("300000"));

        when(promotionRepository.findByCode("SALE2026")).thenReturn(Optional.of(mockPromotion));

        // When
        PromotionCheckResponse res = promotionService.validatePromotion(req);

        // Then
        assertFalse(res.isValid());
        assertEquals("Mã giảm giá đã hết hạn", res.getMessage());
    }

    @Test
    @DisplayName("5. CRUD: Tạo khuyến mãi thành công")
    void createPromotion_Success() {
        PromotionRequest req = new PromotionRequest();
        req.setCode("NEWYEAR");
        req.setDiscountType(DiscountType.FIXED);
        req.setDiscountValue(new BigDecimal("100000"));

        when(promotionRepository.existsByCode("NEWYEAR")).thenReturn(false);
        when(promotionRepository.save(any(Promotion.class))).thenReturn(mockPromotion);

        assertDoesNotThrow(() -> promotionService.createPromotion(req));
        verify(promotionRepository).save(any(Promotion.class));
    }
}