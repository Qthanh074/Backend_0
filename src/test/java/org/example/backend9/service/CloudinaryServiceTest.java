package org.example.backend9.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CloudinaryServiceTest {

    @Mock
    private Cloudinary cloudinary;

    @Mock
    private Uploader uploader;

    @InjectMocks
    private CloudinaryService cloudinaryService;

    @BeforeEach
    void setUp() {
        // Vì đối tượng cloudinary được khởi tạo trong @PostConstruct (hàm init),
        // trong Unit Test ta phải dùng Reflection để đưa mock vào thay thế.
        ReflectionTestUtils.setField(cloudinaryService, "cloudinary", cloudinary);
    }

    @Test
    @DisplayName("Upload: Thành công và trả về URL bảo mật")
    void uploadImage_Success() throws IOException {
        // Given
        MockMultipartFile mockFile = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", "content".getBytes());
        String folderName = "products";
        String expectedUrl = "https://res.cloudinary.com/demo/image/upload/v1/test.jpg";

        // Giả lập luồng: cloudinary.uploader() trả về mockUploader
        when(cloudinary.uploader()).thenReturn(uploader);

        // Giả lập luồng: uploader.upload() trả về Map chứa secure_url
        when(uploader.upload(any(byte[].class), anyMap()))
                .thenReturn(Map.of("secure_url", expectedUrl));

        // When
        String resultUrl = cloudinaryService.uploadImage(mockFile, folderName);

        // Then
        assertNotNull(resultUrl);
        assertEquals(expectedUrl, resultUrl);
        verify(uploader, times(1)).upload(any(byte[].class), anyMap());
    }

    @Test
    @DisplayName("Upload: Thất bại khi có lỗi IO")
    void uploadImage_Fail_IOException() throws IOException {
        // Given
        MockMultipartFile mockFile = new MockMultipartFile("file", "test.jpg", "image/jpeg", "content".getBytes());

        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), anyMap())).thenThrow(new IOException("Connection error"));

        // When & Then
        assertThrows(IOException.class, () -> cloudinaryService.uploadImage(mockFile, "folder"));
    }
}