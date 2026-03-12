package org.example.backend9.service.sales;

import org.example.backend9.dto.request.sales.PromotionRequest;
import org.example.backend9.dto.request.sales.PromotionCheckRequest;
import org.example.backend9.dto.response.sales.PromotionResponse;
import org.example.backend9.dto.response.sales.PromotionCheckResponse;
import org.example.backend9.entity.sales.Promotion;
import org.example.backend9.enums.DiscountType; // ✅ Cần import cái này
import org.example.backend9.repository.sales.PromotionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate; // ✅ Cần import cái này
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PromotionService {

    private final PromotionRepository promotionRepository;

    public PromotionService(PromotionRepository promotionRepository) {
        this.promotionRepository = promotionRepository;
    }

    public List<PromotionResponse> getAllPromotions() {
        return promotionRepository.findAll().stream()
                .map(PromotionResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public PromotionResponse createPromotion(PromotionRequest request) {
        if (promotionRepository.existsByCode(request.getCode())) {
            throw new RuntimeException("Mã khuyến mãi đã tồn tại!");
        }

        Promotion p = new Promotion();
        p.setCode(request.getCode());
        p.setName(request.getName());
        p.setDescription(request.getDescription());
        p.setDiscountType(request.getDiscountType());
        p.setDiscountValue(request.getDiscountValue());
        p.setMinPurchase(request.getMinPurchase() != null ? request.getMinPurchase() : BigDecimal.ZERO);
        p.setMaxDiscount(request.getMaxDiscount());
        p.setStartDate(request.getStartDate());
        p.setEndDate(request.getEndDate());
        p.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);

        return PromotionResponse.fromEntity(promotionRepository.save(p));
    }

    @Transactional
    public PromotionResponse updatePromotion(Integer id, PromotionRequest request) {
        Promotion p = promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Khuyến mãi"));

        if (!p.getCode().equals(request.getCode()) && promotionRepository.existsByCode(request.getCode())) {
            throw new RuntimeException("Mã khuyến mãi đã tồn tại!");
        }

        p.setCode(request.getCode());
        p.setName(request.getName());
        p.setDescription(request.getDescription());
        p.setDiscountType(request.getDiscountType());
        p.setDiscountValue(request.getDiscountValue());
        p.setMinPurchase(request.getMinPurchase() != null ? request.getMinPurchase() : BigDecimal.ZERO);
        p.setMaxDiscount(request.getMaxDiscount());
        p.setStartDate(request.getStartDate());
        p.setEndDate(request.getEndDate());
        if (request.getIsActive() != null) p.setIsActive(request.getIsActive());

        return PromotionResponse.fromEntity(promotionRepository.save(p));
    }

    @Transactional
    public void deletePromotion(Integer id) {
        if (!promotionRepository.existsById(id)) throw new RuntimeException("Không tìm thấy KM");
        promotionRepository.deleteById(id);
    }

    public PromotionCheckResponse validatePromotion(PromotionCheckRequest request) {
        // 1. Tìm mã trong DB
        Promotion p = promotionRepository.findByCode(request.getCode()).orElse(null);
        if (p == null) return errorResponse("Mã giảm giá không tồn tại");

        // 2. Kiểm tra Trạng thái & Thời gian
        if (!p.getIsActive()) return errorResponse("Mã giảm giá đã bị tạm dừng");

        LocalDate now = LocalDate.now();
        if (now.isBefore(p.getStartDate())) return errorResponse("Chương trình chưa bắt đầu");
        if (now.isAfter(p.getEndDate())) return errorResponse("Mã giảm giá đã hết hạn");

        // 3. Kiểm tra Tổng đơn hàng tối thiểu
        BigDecimal minPurchase = p.getMinPurchase() != null ? p.getMinPurchase() : BigDecimal.ZERO;
        if (request.getOrderTotal().compareTo(minPurchase) < 0) {
            return errorResponse("Đơn hàng chưa đạt mức tối thiểu: " + minPurchase);
        }

        // 4. Tính toán số tiền giảm
        BigDecimal discount = BigDecimal.ZERO;
        if (p.getDiscountType() == DiscountType.FIXED) {
            discount = p.getDiscountValue();
        } else { // PERCENTAGE
            // Giảm = Tổng đơn * (% / 100)
            discount = request.getOrderTotal().multiply(p.getDiscountValue()).divide(new BigDecimal(100));
            // Chặn mức giảm tối đa (nếu có)
            if (p.getMaxDiscount() != null && discount.compareTo(p.getMaxDiscount()) > 0) {
                discount = p.getMaxDiscount();
            }
        }

        return PromotionCheckResponse.builder()
                .valid(true)
                .message("Áp dụng mã thành công!")
                .discountAmount(discount)
                .build();
    }

    private PromotionCheckResponse errorResponse(String msg) {
        return PromotionCheckResponse.builder().valid(false).message(msg).discountAmount(BigDecimal.ZERO).build();
    }
}