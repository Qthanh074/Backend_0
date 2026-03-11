package org.example.backend9.service.auth;

import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenBlacklistService {
    // Lưu blacklist trên RAM (Có thể chuyển sang Redis hoặc DB sau này)
    private final Map<String, Long> blacklistedTokens = new ConcurrentHashMap<>();

    public void blacklistToken(String token, long expiryDurationMs) {
        blacklistedTokens.put(token, System.currentTimeMillis() + expiryDurationMs);
    }

    public boolean isTokenBlacklisted(String token) {
        Long expiryTime = blacklistedTokens.get(token);
        if (expiryTime != null) {
            if (expiryTime < System.currentTimeMillis()) {
                blacklistedTokens.remove(token); // Xóa token đã quá hạn khỏi bộ nhớ
                return false;
            }
            return true;
        }
        return false;
    }
}