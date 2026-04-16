package com.tn.grocery_app.filter;

import com.tn.grocery_app.service.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // todo: fetch real user from db instead of just validating the jwt
    // todo: CustomUserDetailsService implementation is needed
    // Utility class for JWT operations (extract + validate)
    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Read Authorization header
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String email;

        // If no Bearer token, skip filter
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extract JWT token
        jwt = authHeader.substring(7);

        // Extract email (subject) from token
        email = jwtUtil.extractEmail(jwt);

        // Proceed only if email exists and user is not already authenticated
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Validate token (email match + expiration check)
            if (jwtUtil.validateToken(jwt, email)) {

                // Create a basic UserDetails object (no DB lookup yet)
                UserDetails userDetails = User.withUsername(email)
                        .password("") // not required for JWT auth
                        .authorities(Collections.emptyList()) // no roles yet
                        .build();

                // Create authentication object
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                // Store authentication in SecurityContext (user is now "logged in")
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // Continue request flow
        filterChain.doFilter(request, response);
    }
}