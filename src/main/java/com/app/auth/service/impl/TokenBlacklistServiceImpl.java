package com.app.auth.service.impl;

import com.app.auth.service.TokenBlacklistService;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Service
public class TokenBlacklistServiceImpl implements TokenBlacklistService {
    
    // In-memory storage for blacklisted tokens with expiration time
    private final Map<String, OffsetDateTime> blacklistedTokens = new ConcurrentHashMap<>();
    
    @Override
    public void blacklistToken(String token) {
        // Store token with current time + 30 days (same as token expiry)
        blacklistedTokens.put(token, OffsetDateTime.now().plusDays(30));
        System.out.println("[DEBUG] Token blacklisted: " + token.substring(0, Math.min(20, token.length())) + "...");
    }
    
    @Override
    public boolean isTokenBlacklisted(String token) {
        OffsetDateTime expiryTime = blacklistedTokens.get(token);
        if (expiryTime == null) {
            return false;
        }
        
        // If token has expired in blacklist, remove it and return false
        if (expiryTime.isBefore(OffsetDateTime.now())) {
            blacklistedTokens.remove(token);
            return false;
        }
        
        return true;
    }
    
    @Override
    public void cleanupExpiredTokens() {
        OffsetDateTime now = OffsetDateTime.now();
        blacklistedTokens.entrySet().removeIf(entry -> entry.getValue().isBefore(now));
        System.out.println("[DEBUG] Cleaned up expired blacklisted tokens. Current count: " + blacklistedTokens.size());
    }
}
