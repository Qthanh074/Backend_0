package org.example.backend9.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PdfServiceTest {

    @Mock
    private TemplateEngine templateEngine;

    @InjectMocks
    private PdfService pdfService;

    @Test
    @DisplayName("PDF: Tạo file PDF từ HTML thành công")
    void generatePdfFromHtml_Success() {
        // Given
        String templateName = "invoice-template";
        Map<String, Object> data = Map.of("customer", "Ngọc", "total", 1000000);

        // Giả lập Thymeleaf trả về một chuỗi HTML đơn giản nhưng hợp lệ cho IText
        String mockHtml = "<html><body><h1>Invoice for Ngọc</h1><p>Total: 1,000,000</p></body></html>";

        when(templateEngine.process(eq(templateName), any(Context.class))).thenReturn(mockHtml);

        // When
        byte[] pdfBytes = pdfService.generatePdfFromHtml(templateName, data);

        // Then
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);

        // Kiểm tra chữ ký file PDF (File Signature)
        // Mọi file PDF đều bắt đầu bằng chuỗi "%PDF-"
        String pdfHeader = new String(pdfBytes, 0, 5);
        assertEquals("%PDF-", pdfHeader, "Mảng byte trả về phải có định dạng của file PDF");
    }

    @Test
    @DisplayName("Error: Ném RuntimeException khi quá trình render bị lỗi")
    void generatePdfFromHtml_ThrowException() {
        // Given
        when(templateEngine.process(anyString(), any(Context.class)))
                .thenThrow(new RuntimeException("Template not found"));

        // When & Then
        assertThrows(RuntimeException.class, () ->
                pdfService.generatePdfFromHtml("error-template", Map.of()));
    }
}