package org.example.backend9.service.core;

import org.example.backend9.dto.request.core.AreaRequest;
import org.example.backend9.dto.response.core.AreaResponse;
import org.example.backend9.entity.core.Area;
import org.example.backend9.enums.EntityStatus;
import org.example.backend9.repository.core.AreaRepository;
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
@Transactional // Đảm bảo dọn sạch DB thật sau khi test xong
class AreaServiceIntegrationTest {

    @Autowired
    private AreaService areaService;

    @Autowired
    private AreaRepository areaRepository;

    // Chặn Google Sheet API để không báo lỗi khởi tạo
    @MockBean
    private org.example.backend9.service.GoogleSheetService googleSheetService;

    // ================= TEST HÀM LẤY DANH SÁCH =================
    @Test
    void getAllAreas_ShouldReturnListWithCorrectData() {
        // 1. Chuẩn bị: Bơm trực tiếp 2 khu vực nháp vào Database thật
        Area area1 = new Area();
        area1.setCode("MB");
        area1.setName("Miền Bắc");
        area1.setStatus(EntityStatus.ACTIVE);
        areaRepository.save(area1);

        Area area2 = new Area();
        area2.setCode("MN");
        area2.setName("Miền Nam");
        area2.setStatus(EntityStatus.ACTIVE);
        areaRepository.save(area2);

        // 2. Thực thi: Gọi hàm Service để lấy danh sách
        List<AreaResponse> result = areaService.getAllAreas();

        // 3. Kiểm tra: Đảm bảo danh sách lấy ra không rỗng và chứa dữ liệu vừa bơm
        assertFalse(result.isEmpty(), "Danh sách không được rỗng");
        assertTrue(result.size() >= 2, "Phải có ít nhất 2 khu vực");

        // Kiểm tra xem mã MB có tồn tại trong kết quả trả về không
        boolean containsMB = result.stream().anyMatch(a -> a.getCode().equals("MB"));
        assertTrue(containsMB, "Phải chứa khu vực có mã MB");
    }

    // ================= TEST HÀM THÊM MỚI =================
    @Test
    void createArea_ShouldSaveToRealDatabase_AndReturnResponse() {
        // 1. Chuẩn bị request
        AreaRequest request = new AreaRequest();
        request.setCode("MT");
        request.setName("Miền Trung");
        request.setDescription("Khu vực test miền Trung");

        // 2. Thực thi
        AreaResponse response = areaService.createArea(request);

        // 3. Kiểm tra kết quả trả về
        assertNotNull(response);
        assertNotNull(response.getId(), "ID phải được DB tự động sinh ra");
        assertEquals("MT", response.getCode()); // Đảm bảo code đã được in hoa
        assertEquals("Miền Trung", response.getName());

        // 4. Lôi thẳng từ Database ra để kiểm chứng (Double Check)
        Area savedArea = areaRepository.findById(response.getId()).orElse(null);
        assertNotNull(savedArea, "Dữ liệu phải thực sự tồn tại trong Database");
        assertEquals("Miền Trung", savedArea.getName());
        assertEquals(EntityStatus.ACTIVE, savedArea.getStatus(), "Status mặc định phải là ACTIVE");
    }

    // ================= TEST HÀM CẬP NHẬT (THÀNH CÔNG) =================
    @Test
    void updateArea_WhenIdExists_ShouldUpdateDatabase() {
        // 1. Bơm 1 dữ liệu nháp vào DB để lấy ID thật
        Area existingArea = new Area();
        existingArea.setCode("OLD");
        existingArea.setName("Tên cũ");
        existingArea.setStatus(EntityStatus.ACTIVE);
        existingArea = areaRepository.save(existingArea); // Save để DB cấp ID

        // 2. Chuẩn bị request cập nhật
        AreaRequest updateRequest = new AreaRequest();
        updateRequest.setCode("NEW_CODE");
        updateRequest.setName("Tên đã sửa");
        updateRequest.setDescription("Mô tả mới");
        updateRequest.setStatus(EntityStatus.INACTIVE);

        // 3. Thực thi cập nhật với ID thật vừa tạo
        AreaResponse response = areaService.updateArea(existingArea.getId(), updateRequest);

        // 4. Kiểm tra
        assertNotNull(response);
        assertEquals("NEW_CODE", response.getCode());
        assertEquals("Tên đã sửa", response.getName());
        assertEquals(EntityStatus.INACTIVE, response.getStatus());

        // Móc từ DB ra xem có bị sửa thật không
        Area updatedAreaInDb = areaRepository.findById(existingArea.getId()).orElseThrow();
        assertEquals("NEW_CODE", updatedAreaInDb.getCode());
    }

    // ================= TEST HÀM CẬP NHẬT (THẤT BẠI DO SAI ID) =================
    @Test
    void updateArea_WhenIdDoesNotExist_ShouldThrowException() {
        // Chuẩn bị request
        AreaRequest request = new AreaRequest();
        request.setCode("TEST");
        request.setName("Test Exception");

        // Truyền bừa 1 cái ID không có thật (ví dụ 99999) và chờ đợi lỗi văng ra
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            areaService.updateArea(99999, request);
        });

        // Kiểm tra xem câu chửi của hệ thống có đúng như mình viết trong Service không
        assertEquals("Không tìm thấy khu vực", exception.getMessage());
    }

    // ================= TEST HÀM XÓA MỀM (THÀNH CÔNG) =================
    @Test
    void deleteArea_WhenIdExists_ShouldSetStatusToInactive() {
        // 1. Tạo dữ liệu nháp
        Area area = new Area();
        area.setCode("DEL_TEST");
        area.setName("Khu vực chuẩn bị xóa");
        area.setStatus(EntityStatus.ACTIVE);
        area = areaRepository.save(area);

        // 2. Thực thi xóa
        areaService.deleteArea(area.getId());

        // 3. Kiểm tra DB xem status đã thành INACTIVE chưa (Chứ không bị mất hẳn)
        Area deletedArea = areaRepository.findById(area.getId()).orElse(null);
        assertNotNull(deletedArea, "Dữ liệu không được xóa cứng khỏi DB");
        assertEquals(EntityStatus.INACTIVE, deletedArea.getStatus(), "Status phải chuyển thành INACTIVE");
    }

    // ================= TEST HÀM XÓA (THẤT BẠI DO SAI ID) =================
    @Test
    void deleteArea_WhenIdDoesNotExist_ShouldThrowException() {
        // Gọi lệnh xóa với ID fake
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            areaService.deleteArea(88888);
        });

        assertEquals("Không tìm thấy khu vực", exception.getMessage());
    }
}