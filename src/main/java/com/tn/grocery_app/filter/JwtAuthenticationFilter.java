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
    // todo: what is a filter and how does it work
    // Utility class for JWT operations (extract + validate)
    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Get Authorization header from request
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String phone;

        // If header is missing or doesn't start with "Bearer ", skip this filter
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extract token (remove "Bearer " prefix)
        jwt = authHeader.substring(7);

        // Extract phone (subject) from token
        phone = jwtUtil.extractPhone(jwt);

        // Proceed only if:
        // 1. phone exists in token
        // 2. no authentication is already set in context
        if (phone != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Validate token (check phone + expiration)
            if (jwtUtil.validateToken(jwt, phone)) {

                // Create a minimal UserDetails object (no password, no roles for now)
                UserDetails userDetails = User.withUsername(phone)
                        .password("") // password not needed here
                        .authorities(Collections.emptyList()) // no roles assigned
                        .build();

                // Create authentication token
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                // Set authentication in SecurityContext (user is now authenticated)
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // Continue filter chain
        filterChain.doFilter(request, response);
    }
}