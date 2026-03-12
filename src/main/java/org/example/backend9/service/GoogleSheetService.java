package org.example.backend9.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class GoogleSheetService {

    @Value("${google.credentials.file.path}")
    private Resource credentialsResource;

    @Value("${google.sheets.spreadsheet.id}")
    private String spreadsheetId;

    @Value("${google.drive.folder.id}")
    private String driveFolderId;

    private Sheets sheetsService;
    private Drive driveService;

    @PostConstruct
    public void init() {
        try {
            GoogleCredentials credentials = GoogleCredentials.fromStream(credentialsResource.getInputStream())
                    .createScoped(Arrays.asList(SheetsScopes.SPREADSHEETS, DriveScopes.DRIVE_FILE));

            this.sheetsService = new Sheets.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(),
                    new HttpCredentialsAdapter(credentials))
                    .setApplicationName("SmartRetail-WMS")
                    .build();

            this.driveService = new Drive.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(),
                    new HttpCredentialsAdapter(credentials))
                    .setApplicationName("SmartRetail-WMS")
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Không thể khởi tạo Google API. Vui lòng kiểm tra lại file credentials: " + e.getMessage(), e);
        }
    }

    // --- 1. GOOGLE SHEETS: Ghi 1 dòng dữ liệu (VD: Hóa đơn mới) ---
    public void appendRowToSheet(String sheetName, List<Object> values) {
        try {
            String range = sheetName + "!A:A"; // Chèn vào dòng trống tiếp theo
            ValueRange body = new ValueRange().setValues(Collections.singletonList(values));

            sheetsService.spreadsheets().values()
                    .append(spreadsheetId, range, body)
                    .setValueInputOption("USER_ENTERED")
                    .execute();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi ghi Google Sheets: " + e.getMessage());
        }
    }

    // --- 2. GOOGLE DRIVE: Upload file (Excel/PDF) lên thư mục ---
    public String uploadFileToDrive(String fileName, String mimeType, byte[] fileBytes) {
        try {
            File fileMetadata = new File();
            fileMetadata.setName(fileName);
            fileMetadata.setParents(Collections.singletonList(driveFolderId)); // Thả vào đúng Folder

            InputStreamContent mediaContent = new InputStreamContent(
                    mimeType,
                    new ByteArrayInputStream(fileBytes)
            );

            File file = driveService.files().create(fileMetadata, mediaContent)
                    .setFields("id, webViewLink")
                    .execute();

            return file.getWebViewLink(); // Trả về link để xem file
        } catch (Exception e) {
            throw new RuntimeException("Lỗi upload Google Drive: " + e.getMessage());
        }
    }
}