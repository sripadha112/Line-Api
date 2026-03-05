package com.app.auth.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Lightweight API Logging Interceptor
 * - Logs API requests with minimal overhead
 * - Shows timestamp and controller method name
 * - Logs errors if any exception occurs
 */
@Component
public class ApiLoggingInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(ApiLoggingInterceptor.class);
    private static final String HANDLER_METHOD_ATTR = "handlerMethod";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    /**
     * Pre-handle: Called before the controller method executes
     * Extract handler method information
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Extract handler method information
        String handlerInfo = "Unknown";
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            String className = handlerMethod.getBeanType().getSimpleName();
            String methodName = handlerMethod.getMethod().getName();
            handlerInfo = className + "." + methodName + "()";
        }
        request.setAttribute(HANDLER_METHOD_ATTR, handlerInfo);
        
        return true; // Continue with request processing
    }

    /**
     * Post-handle: Called after controller method executes but before view rendering
     * Still lightweight - minimal processing here
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        // Optional: Can add logic here if needed
    }

    /**
     * After-completion: Called after the complete request has finished
     * Log the API call with timestamp and controller method
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        String handlerInfo = (String) request.getAttribute(HANDLER_METHOD_ATTR);
        String timestamp = LocalDateTime.now().format(formatter);
        int statusCode = response.getStatus();
        
        // Check if there's an error (exception or HTTP error status)
        boolean isError = ex != null || statusCode >= 400;
        
        if (isError) {
            // Log error - error comes first
            if (ex != null) {
                logger.error("Error: {} | Status: {} | Timestamp: {} | Controller: {}", 
                    ex.getMessage(), statusCode, timestamp, handlerInfo);
            } else {
                logger.error("Error: HTTP {} | Timestamp: {} | Controller: {}", 
                    statusCode, timestamp, handlerInfo);
            }
        } else {
            // Log success
            logger.info("Timestamp: {} | Controller: {}", timestamp, handlerInfo);
        }
    }
}
