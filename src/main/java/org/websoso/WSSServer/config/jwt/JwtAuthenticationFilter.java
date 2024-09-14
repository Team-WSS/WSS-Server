package org.websoso.WSSServer.config.jwt;

import static org.websoso.WSSServer.config.jwt.JwtValidationType.EXPIRED_ACCESS;
import static org.websoso.WSSServer.config.jwt.JwtValidationType.VALID_ACCESS;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final static String TOKEN_PREFIX = "Bearer ";
    private final JWTUtil jwtUtil;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            final String token = getJwtFromRequest(request);
            final JwtValidationType validationResult = jwtUtil.validateJWT(token);
            if (validationResult == VALID_ACCESS) {
                Long memberId = jwtUtil.getUserIdFromJwt(token);
                UserAuthentication authentication = new UserAuthentication(memberId.toString(), null, null);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else if (validationResult == EXPIRED_ACCESS) {
                handleExpiredAccessToken(request, response);
                return;
            }
        } catch (Exception exception) {
            try {
                throw new Exception();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(TOKEN_PREFIX)) {
            return bearerToken.substring(TOKEN_PREFIX.length());
        }
        return null;
    }

    private void handleExpiredAccessToken(HttpServletRequest request,
                                          HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter()
                .write("{\"code\": \"AUTH-000\", \"message\": \"Access Token Expired. Use Refresh Token to reissue.\"}");
    }
}
