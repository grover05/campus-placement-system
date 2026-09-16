package com.pms.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

/**
 * Consolidated JWT utilities and request filter.
 */
public final class JwtSecurity {

    private JwtSecurity() {}

    @Component
    public static class JwtUtil {

        @Value("${app.jwt.secret}")
        private String secret;

        @Value("${app.jwt.expiration-ms}")
        private long expirationMs;

        private SecretKey key() {
            return Keys.hmacShaKeyFor(secret.getBytes());
        }

        public String generateToken(String email, String role) {
            Date now = new Date();
            Date expiry = new Date(now.getTime() + expirationMs);
            return Jwts.builder()
                    .subject(email)
                    .claims(Map.of("role", role))
                    .issuedAt(now)
                    .expiration(expiry)
                    .signWith(key())
                    .compact();
        }

        public String extractEmail(String token) {
            return extractClaim(token, Claims::getSubject);
        }

        public String extractRole(String token) {
            return extractAllClaims(token).get("role", String.class);
        }

        public boolean isTokenValid(String token, String email) {
            return email.equals(extractEmail(token)) && !isExpired(token);
        }

        private boolean isExpired(String token) {
            return extractClaim(token, Claims::getExpiration).before(new Date());
        }

        private <T> T extractClaim(String token, Function<Claims, T> resolver) {
            return resolver.apply(extractAllClaims(token));
        }

        private Claims extractAllClaims(String token) {
            return Jwts.parser()
                    .verifyWith(key())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        }
    }

    @Component
    @RequiredArgsConstructor
    public static class JwtAuthFilter extends OncePerRequestFilter {

        private final JwtUtil jwtUtil;
        private final CustomUserDetailsService userDetailsService;

        @Override
        protected void doFilterInternal(@NonNull HttpServletRequest request,
                                         @NonNull HttpServletResponse response,
                                         @NonNull FilterChain filterChain) throws ServletException, IOException {

            String authHeader = request.getHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }

            String token = authHeader.substring(7);

            try {
                String email = jwtUtil.extractEmail(token);

                if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                    if (jwtUtil.isTokenValid(token, email)) {
                        UsernamePasswordAuthenticationToken authToken =
                                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                }
            } catch (Exception ex) {
                SecurityContextHolder.clearContext();
            }

            filterChain.doFilter(request, response);
        }
    }
}
