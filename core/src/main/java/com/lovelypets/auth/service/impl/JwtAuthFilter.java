package com.lovelypets.auth.service.impl;


import com.lovelypets.auth.repository.TokenEntryRepository;
import com.lovelypets.auth.service.JwtService;
import com.lovelypets.entities.TokenEntry;
import com.lovelypets.enums.TokenType;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * JWT-фильтр: извлекает Bearer-токен из Authorization заголовка,
 * валидирует его и устанавливает Authentication в SecurityContext.
 *
 * <p>Работает только с ACCESS-токенами. Refresh-токены принимаются
 * только на {@code /api/v1/auth/refresh} и не должны использоваться
 * для авторизации запросов.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String AUTH_HEADER   = "Authorization";

    private final JwtService            jwtService;
    private final JwtServiceImpl        jwtServiceImpl;
    private final TokenEntryRepository  tokenEntryRepository;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader(AUTH_HEADER);
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(BEARER_PREFIX.length());

        // 1. Проверяем подпись и expiry
        if (!jwtService.isTokenValid(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Проверяем что это ACCESS-токен
        try {
            TokenType tokenType = jwtServiceImpl.extractTokenType(token);
            if (tokenType != TokenType.ACCESS) {
                filterChain.doFilter(request, response);
                return;
            }
        } catch (Exception e) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Проверяем не отозван ли токен в БД
        Optional<TokenEntry> entry = tokenEntryRepository.findByValue(token);
        if (entry.isEmpty() || entry.get().isRevoked()) {
            filterChain.doFilter(request, response);
            return;
        }

        // 4. Формируем Authentication
        String email    = jwtService.extractEmail(token);
        String userType = jwtServiceImpl.extractUserType(token).name();

        // Роль формируем из userType — ROLE_CLIENT или ROLE_STAFF
        List<SimpleGrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_" + userType)
        );

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(email, null, authorities);
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }
}
