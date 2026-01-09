package com.nebula.commerce.modules.auth.filter;

import com.nebula.commerce.infrastructure.web.JwtUtils;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        // 1. 快速检查：如果没有 Token，直接放行（交给 SecurityConfig 的 authenticated() 拦截）
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. 尝试解析 Token
        jwt = authHeader.substring(7);
        try {
            username = jwtUtils.extractUsername(jwt);

            // 3. 如果 Token 有效且上下文未认证
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // 这里每次请求都查库，虽然保证了实时性，但高并发下建议加 Redis 缓存
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                if (jwtUtils.validateToken(jwt, userDetails.getUsername())) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (ExpiredJwtException e) {
            // Token 过期是常见情况，使用 Debug 级别日志
            log.debug("JWT Token 已过期: {}", e.getMessage());
        } catch (Exception e) {
            // 其他解析错误（如签名被篡改），记录警告
            log.warn("JWT 解析失败 IP: {}, 原因: {}", request.getRemoteAddr(), e.getMessage());
        }

        // 4. 继续过滤器链
        filterChain.doFilter(request, response);
    }
}