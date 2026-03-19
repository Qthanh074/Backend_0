package org.example.backend9.service;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ExcelServiceTest {

    @InjectMocks
    private ExcelService excelService;

    private List<String> headers;
    private List<List<Object>> dataRows;

    @BeforeEach
    void setUp() {
        headers = List.of("ID", "Tên", "Giá", "Còn hàng");
        dataRows = List.of(
                List.of(1, "Sản phẩm A", 50000.0, true),
                List.of(2, "Sản phẩm B", 75000.5, false)
        );
    }

    @Test
    @DisplayName("Export: Xuất file Excel thành công và đúng dữ liệu")
    void exportToExcel_Success() throws IOException {
        // When
        byte[] result = excelService.exportToExcel("Sản phẩm", headers, dataRows);

        // Then
        assertNotNull(result);
        assertTrue(result.length > 0);

        // Đọc lại byte[] để kiểm tra nội dung bên trong file Excel
        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(result))) {
            Sheet sheet = workbook.getSheet("Sản phẩm");
            assertNotNull(sheet);

            // 1. Kiểm tra Header (Dòng 0)
            Row headerRow = sheet.getRow(0);
            assertEquals("ID", headerRow.getCell(0).getStringCellValue());
            assertEquals("Tên", headerRow.getCell(1).getStringCellValue());

            // 2. Kiểm tra Dữ liệu dòng 1 (Sản phẩm A)
            Row dataRow1 = sheet.getRow(1);
            assertEquals(1.0, dataRow1.getCell(0).getNumericCellValue()); // POI đọc số là Double
            assertEquals("Sản phẩm A", dataRow1.getCell(1).getStringCellValue());
            assertEquals(50000.0, dataRow1.getCell(2).getNumericCellValue());
            assertTrue(dataRow1.getCell(3).getBooleanCellValue());

            // 3. Kiểm tra tổng số dòng (Header + 2 dòng data = 3 dòng)
            assertEquals(3, sheet.getPhysicalNumberOfRows());
        }
    }

    @Test
    @DisplayName("Export: Xử lý trường hợp dữ liệu có giá trị null")
    void exportToExcel_WithNullValue() throws IOException {
        // Sửa List.of thành Arrays.asList để cho phép giá trị null
        List<List<Object>> dataWithNull = List.of(
                java.util.Arrays.asList(3, null, 0, true)
        );

        byte[] result = excelService.exportToExcel("Test Null", headers, dataWithNull);

        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(result))) {
            Sheet sheet = workbook.getSheet("Test Null");
            Row row = sheet.getRow(1);

            // Kiểm tra xem logic trong ExcelService có chuyển null thành "" không
            assertEquals("", row.getCell(1).getStringCellValue());
        }
    }
}