package org.example.backend9.service.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TokenBlacklistServiceTest {

    private TokenBlacklistService blacklistService;

    @BeforeEach
    void setUp() {
        // Khởi tạo service trước mỗi bài test
        blacklistService = new TokenBlacklistService();
    }

    @Test
    @DisplayName("Token chưa bị blacklist thì phải trả về false")
    void shouldReturnFalseWhenTokenIsNotBlacklisted() {
        String token = "some.valid.token";
        assertFalse(blacklistService.isTokenBlacklisted(token));
    }

    @Test
    @DisplayName("Token đã đưa vào blacklist thì phải trả về true")
    void shouldReturnTrueWhenTokenIsBlacklisted() {
        String token = "logout.token.123";
        long expiry = 10000; // 10 giây

        blacklistService.blacklistToken(token, expiry);

        assertTrue(blacklistService.isTokenBlacklisted(token));
    }

    @Test
    @DisplayName("Token đã hết hạn trong blacklist thì phải tự xóa và trả về false")
    void shouldReturnFalseAndRemoveWhenTokenIsExpired() throws InterruptedException {
        String token = "expired.token";
        long expiry = 100; // 100 milis (rất nhanh)

        blacklistService.blacklistToken(token, expiry);

        // Chờ 200ms để chắc chắn token đã hết hạn
        Thread.sleep(200);

        assertFalse(blacklistService.isTokenBlacklisted(token), "Token hết hạn phải coi như không bị blacklist nữa");
    }
}