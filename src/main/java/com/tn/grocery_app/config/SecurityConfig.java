package com.tn.grocery_app.config;

import com.tn.grocery_app.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity // enables Spring Security for the application
@RequiredArgsConstructor
public class SecurityConfig {

    // Custom JWT filter that runs before Spring Security authentication filter
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    // Handles unauthorized access (returns 401 instead of default Spring response)
    private final JwtAuthEntryPoint jwtAuthEntryPoint;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF since we are using stateless JWT authentication
                .csrf(AbstractHttpConfigurer::disable)

                // Define endpoint access rules
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**").permitAll() // public endpoints (login/register)
                        .anyRequest().authenticated() // all other endpoints require authentication
                )

                // Handle authentication errors (invalid/missing token → 401 response)
                .exceptionHandling(ex ->
                        ex.authenticationEntryPoint(jwtAuthEntryPoint)
                )

                // Make session stateless (no server-side session storage)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Add JWT filter before Spring's default authentication filter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}