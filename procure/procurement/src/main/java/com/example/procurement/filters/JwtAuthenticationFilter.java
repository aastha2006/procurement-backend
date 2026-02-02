package com.example.procurement.filters;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    public JwtAuthenticationFilter(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            final String requestURI = request.getRequestURI();

            if ("OPTIONS".equalsIgnoreCase(request.getMethod()) ||
                    requestURI.startsWith("/api/auth/forgot-password") ||
                    requestURI.endsWith("/register") ||
                    requestURI.endsWith("/login") ||
                    requestURI.endsWith("/refresh-token") ||
                    requestURI.startsWith("/swagger-ui") ||
                    requestURI.startsWith("/api/auth/update-password/") ||
                    requestURI.startsWith("/v3/api-docs") ||
                    requestURI.startsWith("/api/vendors/") ||
                    requestURI.startsWith("/api/members/") ||
                    requestURI.startsWith("/api/assessment/") ||
                    requestURI.startsWith("/api/auth/debug-session/") || // Allowed for debugging
                    requestURI.startsWith("/api/workflow-executions/")) {
                filterChain.doFilter(request, response);
                return;
            }

            final String authorizationHeader = request.getHeader("Authorization");

            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                System.out.println("DEBUG: Auth failure for URI: " + requestURI);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Missing or invalid authorization header for URI: " + requestURI);
                return;
            }

            String token = authorizationHeader.substring(7);
            System.out.println("DEBUG: Processing token for URI: " + requestURI);

            // Extract claims using JwtUtils
            Claims claims = jwtUtils.extractClaim(token, c -> c);
            String username = claims.getSubject();
            System.out.println("DEBUG: Token valid. Subject: " + username);

            // Extract roles from token and convert to SimpleGrantedAuthority
            List<String> roles = jwtUtils.extractRoles(token);
            System.out.println("DEBUG: Extracted roles: " + roles);

            if (roles != null) {
                List<SimpleGrantedAuthority> authorities = roles.stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(username,
                        null,
                        authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                System.out.println("DEBUG: Authentication set in SecurityContext for user: " + username);
            } else {
                System.out.println("DEBUG: No roles found in token");
            }

            request.setAttribute("claims", claims);

            filterChain.doFilter(request, response);

        } catch (io.jsonwebtoken.MalformedJwtException e) {
            System.out.println("DEBUG: Malformed JWT: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("Malformed JWT");
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            System.out.println("DEBUG: Expired JWT: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("JWT token has expired");
        } catch (Exception e) {
            System.out.println("DEBUG: Auth Filter Exception: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("Forbidden: " + e.getMessage());
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.startsWith("/swagger-ui")
                || uri.startsWith("/v3/api-docs")
                || uri.endsWith("/login")
                || uri.endsWith("/register")
                || uri.startsWith("/api/auth/debug-session/") // Skip filter entirely for debug
                || uri.endsWith("/version") // Version check
                || uri.equals("/api/PurchaseOrder/SapPo")
                || "OPTIONS".equalsIgnoreCase(request.getMethod());
    }

}
