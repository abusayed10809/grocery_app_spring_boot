package com.tn.grocery_app.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JwtAuthEntryPoint implements AuthenticationEntryPoint {

    // This method is triggered whenever an unauthenticated user
    // tries to access a protected endpoint
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException)
            throws IOException {

        // Send HTTP 401 response (Unauthorized)
        // This means: user is not authenticated or token is missing/invalid
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
    }
}