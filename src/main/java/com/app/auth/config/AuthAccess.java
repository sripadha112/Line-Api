package com.app.auth.config;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.Objects;

public final class AuthAccess {
    private AuthAccess() {}

    public static Long currentUserId() {
        Object value = currentClaim("userId");
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String text) {
            return Long.parseLong(text);
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing authenticated user");
    }

    public static String currentRole() {
        Object value = currentClaim("role");
        return value == null ? null : value.toString();
    }

    public static void requireSelf(Long userId) {
        if (!Objects.equals(currentUserId(), userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only access your own account");
        }
    }

    public static void requireSelfOrDoctor(Long userId) {
        if ("DOCTOR".equalsIgnoreCase(currentRole())) {
            return;
        }
        requireSelf(userId);
    }

    private static Object currentClaim(String name) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }

        Object details = authentication.getDetails();
        if (details instanceof Map<?, ?> claims) {
            return claims.get(name);
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication details missing");
    }
}
