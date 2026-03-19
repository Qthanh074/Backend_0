package org.example.backend9.service;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.AppendValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoogleSheetServiceTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Sheets sheetsService;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Drive driveService;

    @InjectMocks
    private GoogleSheetService googleSheetService;

    @BeforeEach
    void setUp() {
        // Dùng Reflection để đưa mock vào vì các biến này private và khởi tạo trong @PostConstruct
        ReflectionTestUtils.setField(googleSheetService, "sheetsService", sheetsService);
        ReflectionTestUtils.setField(googleSheetService, "driveService", driveService);
        ReflectionTestUtils.setField(googleSheetService, "spreadsheetId", "mock-spreadsheet-id");
        ReflectionTestUtils.setField(googleSheetService, "driveFolderId", "mock-folder-id");
    }

    @Test
    @DisplayName("Sheets: Ghi dòng dữ liệu thành công")
    void appendRowToSheet_Success() throws IOException {
        // Given
        List<Object> rowData = List.of("Data 1", "Data 2");

        // Mock luồng gọi hàm lồng nhau của Google Sheets API
        // sheetsService.spreadsheets().values().append(...).setValueInputOption(...).execute()
        when(sheetsService.spreadsheets().values()
                .append(anyString(), anyString(), any(ValueRange.class))
                .setValueInputOption(anyString())
                .execute())
                .thenReturn(new AppendValuesResponse());

        // When & Then
        assertDoesNotThrow(() -> googleSheetService.appendRowToSheet("Sheet1", rowData));
        verify(sheetsService.spreadsheets().values(), times(1)).append(eq("mock-spreadsheet-id"), anyString(), any(ValueRange.class));
    }

    @Test
    @DisplayName("Drive: Upload file lên Drive và trả về link")
    void uploadFileToDrive_Success() throws IOException {
        // Given
        byte[] content = "test content".getBytes();
        String expectedLink = "https://drive.google.com/mock-link";
        File mockFile = new File().setWebViewLink(expectedLink);

        // Mock luồng gọi hàm của Drive API
        // driveService.files().create(...).setFields(...).execute()
        when(driveService.files().create(any(File.class), any())
                .setFields(anyString())
                .execute())
                .thenReturn(mockFile);

        // When
        String resultLink = googleSheetService.uploadFileToDrive("test.txt", "text/plain", content);

        // Then
        assertEquals(expectedLink, resultLink);
        verify(driveService.files(), times(1)).create(any(File.class), any());
    }

    @Test
    @DisplayName("Error: Ném RuntimeException khi Google API lỗi")
    void googleApi_HandleException() throws IOException {
        when(sheetsService.spreadsheets().values()
                .append(anyString(), anyString(), any(ValueRange.class)))
                .thenThrow(new IOException("API Error"));

        assertThrows(RuntimeException.class, () -> googleSheetService.appendRowToSheet("Sheet1", List.of()));
    }
}