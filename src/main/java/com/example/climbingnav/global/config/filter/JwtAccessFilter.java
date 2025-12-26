package com.example.climbingnav.global.config.filter;

import com.example.climbingnav.auth.entity.User;
import com.example.climbingnav.auth.repository.UserRepository;
import com.example.climbingnav.global.base.UserVo;
import com.example.climbingnav.global.jwt.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtAccessFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String accessToken = header.substring(7);

            if(!jwtUtil.validateToken(accessToken)) {
                throw new ServletException("Invalid access");
            }

            Claims claims = jwtUtil.parse(accessToken);
            String userId = claims.getSubject();
            User user = userRepository.findById(Long.valueOf(userId))
                            .orElseThrow(() -> new RuntimeException("User not found.."));

            UserVo userVo = new UserVo(
                    user.getId(),
                    user.getEmail(),
                    user.getNickname(),
                    user.getAvatarUrl()
            );

            log.info("userId={}, 해당 유저 filter 진입", user.getEmail());

            Authentication auth = new UsernamePasswordAuthenticationToken(userVo, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getServletPath().equals("/api/auth/refresh");
    }
}
