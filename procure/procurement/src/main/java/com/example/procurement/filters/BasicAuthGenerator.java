package com.example.procurement.filters;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;

import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class BasicAuthGenerator extends OncePerRequestFilter {

    @Value("${authUser}")
    private String expectedUsername;

    @Value("${authPassword}")
    private String encodedPassword;

    private final PasswordEncoder passwordEncoder;

    public BasicAuthGenerator(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if (!request.getRequestURI().equals("/api/PurchaseOrder/SapPo")) {
            filterChain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Basic ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Missing or invalid Authorization header for Basic Auth");
            return;
        }

        String base64Credentials = header.substring("Basic ".length());
        String credentials = new String(Base64.getDecoder().decode(base64Credentials));
        String[] values = credentials.split(":", 2);

        System.out.println("Received username: " + values[0]);
        System.out.println("Received password: " + values[1]);
        System.out.println("Expected username: " + expectedUsername);
        System.out.println("Matches password: " + passwordEncoder.matches(values[1], encodedPassword));
        //System.out.println("Matches password: " +  encodedPassword);

        // if (values.length != 2) {
        //     response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        //     response.getWriter().write("Invalid Basic Auth format");
        //     return;
        // }

        // String inputUsername = values[0];
        // String inputPassword = values[1];

        // if (!expectedUsername.equals(inputUsername) ||!encodedPassword.equals(inputPassword)) {
        //     response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        //     response.getWriter().write("Invalid username or password");
        //     return;
        // }
        // if (values.length != 2 || !expectedUsername.equals(values[0]) ||
        //     !passwordEncoder.matches(values[1], encodedPassword)) {
        //     response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        //     response.getWriter().write("Invalid username or password");
        //     return;
        // }

        UsernamePasswordAuthenticationToken auth =
            new UsernamePasswordAuthenticationToken(values[0], null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));

        SecurityContextHolder.getContext().setAuthentication(auth);
        filterChain.doFilter(request, response);
    }

    @Override
protected boolean shouldNotFilter(HttpServletRequest request) {
    return !request.getRequestURI().equals("/api/PurchaseOrder/SapPo");
}
}
