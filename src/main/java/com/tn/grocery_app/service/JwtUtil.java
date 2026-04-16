package com.tn.grocery_app.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtUtil {
    // todo: update the key. put the key in the env.
    // Secret key used to sign JWT (generated once when app starts)
    // ⚠️ In production, store this securely (env variable / config), not like this
    private final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    // todo: use email to generate token ?? need to check this one
    // Generate JWT token using user's phone number as subject
    public String generateToken(String phone) {
        return Jwts.builder()
                .setSubject(phone) // unique identifier (user)
                .setIssuedAt(new Date()) // token creation time
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // valid for 10 hours
                .signWith(key) // sign token with secret key
                .compact();
    }

    // Extract phone (subject) from token
    public String extractPhone(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Extract expiration date from token
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Generic method to extract any claim using a resolver function
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Parse token and retrieve all claims (payload data)
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key) // verify signature using same key
                .build()
                .parseClaimsJws(token) // parse token
                .getBody(); // return payload (claims)
    }

    // Check if token is expired
    public Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // Validate token:
    // 1. Phone matches
    // 2. Token is not expired
    public Boolean validateToken(String token, String phone) {
        final String extractedPhone = extractPhone(token);
        return (extractedPhone.equals(phone) && !isTokenExpired(token));
    }
}