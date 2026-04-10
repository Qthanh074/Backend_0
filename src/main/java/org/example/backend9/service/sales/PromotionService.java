package org.example.backend9.service.sales;

import org.example.backend9.dto.request.sales.PromotionRequest;
import org.example.backend9.dto.request.sales.PromotionCheckRequest;
import org.example.backend9.dto.response.sales.PromotionResponse;
import org.example.backend9.dto.response.sales.PromotionCheckResponse;
import org.example.backend9.entity.sales.Promotion;
import org.example.backend9.enums.DiscountType;
import org.example.backend9.repository.sales.PromotionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PromotionService {

    private final PromotionRepository promotionRepository;

    public PromotionService(PromotionRepository promotionRepository) {
        this.promotionRepository = promotionRepository;
    }

    // 🟢 Thêm lại hàm bị thiếu để sửa lỗi getAllPromotions()
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
        updatePromotionFields(p, request); // Dùng hàm dùng chung để gán dữ liệu

        return PromotionResponse.fromEntity(promotionRepository.save(p));
    }

    // 🟢 Thêm lại hàm bị thiếu để sửa lỗi updatePromotion()
    @Transactional
    public PromotionResponse updatePromotion(Integer id, PromotionRequest request) {
        Promotion p = promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Khuyến mãi"));

        if (!p.getCode().equals(request.getCode()) && promotionRepository.existsByCode(request.getCode())) {
            throw new RuntimeException("Mã khuyến mãi đã tồn tại!");
        }

        updatePromotionFields(p, request); // Cập nhật dữ liệu

        return PromotionResponse.fromEntity(promotionRepository.save(p));
    }

    // 🟢 Thêm lại hàm bị thiếu để sửa lỗi deletePromotion()
    @Transactional
    public void deletePromotion(Integer id) {
        if (!promotionRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy Khuyến mãi");
        }
        promotionRepository.deleteById(id);
    }

    // Logic tính toán cho cả 2 loại mã % (có giới hạn và không giới hạn)
    public PromotionCheckResponse validatePromotion(PromotionCheckRequest request) {
        Promotion p = promotionRepository.findByCode(request.getCode()).orElse(null);
        if (p == null) return errorResponse("Mã giảm giá không tồn tại");

        if (!p.getIsActive()) return errorResponse("Mã giảm giá đã bị tạm dừng");

        LocalDate now = LocalDate.now();
        if (now.isBefore(p.getStartDate())) return errorResponse("Chương trình chưa bắt đầu");
        if (now.isAfter(p.getEndDate())) return errorResponse("Mã giảm giá đã hết hạn");

        if (request.getOrderTotal().compareTo(p.getMinPurchase()) < 0) {
            return errorResponse("Đơn hàng chưa đạt mức tối thiểu: " + p.getMinPurchase());
        }

        BigDecimal discount = BigDecimal.ZERO;
        if (p.getDiscountType() == DiscountType.FIXED) {
            discount = p.getDiscountValue();
        } else { // PERCENTAGE
            discount = request.getOrderTotal().multiply(p.getDiscountValue())
                    .divide(new BigDecimal(100), 0, RoundingMode.HALF_UP);

            // 🟢 Logic: Nếu maxDiscount > 0 thì mới giới hạn (Loại % có giới hạn)
            // Nếu maxDiscount là null hoặc 0 thì không chặn (Loại % không giới hạn)
            if (p.getMaxDiscount() != null && p.getMaxDiscount().compareTo(BigDecimal.ZERO) > 0) {
                if (discount.compareTo(p.getMaxDiscount()) > 0) {
                    discount = p.getMaxDiscount();
                }
            }
        }

        return PromotionCheckResponse.builder()
                .valid(true)
                .message("Áp dụng mã thành công!")
                .discountAmount(discount)
                .build();
    }

    // Hàm dùng chung để gán dữ liệu từ Request sang Entity
    private void updatePromotionFields(Promotion p, PromotionRequest request) {
        p.setCode(request.getCode());
        p.setName(request.getName());
        p.setDescription(request.getDescription());
        p.setDiscountType(request.getDiscountType());
        p.setDiscountValue(request.getDiscountValue());
        p.setApplyFor(request.getApplyFor() != null ? request.getApplyFor() : "ALL");

        // Đảm bảo không bao giờ bị Null khi lưu vào DB
        p.setMinPurchase(request.getMinPurchase() != null ? request.getMinPurchase() : BigDecimal.ZERO);

        // Giảm tối đa: Nếu không nhập hoặc nhập <= 0 thì coi như không giới hạn (null)
        if (request.getMaxDiscount() != null && request.getMaxDiscount().compareTo(BigDecimal.ZERO) > 0) {
            p.setMaxDiscount(request.getMaxDiscount());
        } else {
            p.setMaxDiscount(null);
        }

        p.setStartDate(request.getStartDate());
        p.setEndDate(request.getEndDate());
        p.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
    }

    private PromotionCheckResponse errorResponse(String msg) {
        return PromotionCheckResponse.builder().valid(false).message(msg).discountAmount(BigDecimal.ZERO).build();
    }
}