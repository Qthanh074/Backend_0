package org.example.backend9.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.util.Map;

@Service
public class PdfService {

    @Autowired
    private TemplateEngine templateEngine;

    public byte[] generatePdfFromHtml(String templateName, Map<String, Object> data) {
        // Dùng try-with-resources để tự động close stream
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Context context = new Context();
            context.setVariables(data);
            String htmlContent = templateEngine.process(templateName, context);

            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(htmlContent);
            renderer.layout();
            renderer.createPDF(outputStream);

            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi tạo PDF: " + e.getMessage(), e);
        }
    }
}