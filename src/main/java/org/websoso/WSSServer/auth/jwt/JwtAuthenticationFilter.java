package org.websoso.WSSServer.auth.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.websoso.WSSServer.exception.error.CustomAuthError;
import org.websoso.common.exception.ErrorResult;
import org.websoso.support.logging.request.RequestLoggingFilter;

/** JWT 인증을 처리하고 인증된 사용자 식별자를 요청 로그 컨텍스트에 전달한다. */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final static String TOKEN_PREFIX = "Bearer ";
    private final JWTUtil jwtUtil;
    private final ObjectMapper objectMapper;

    /** Access Token을 검증하고 인증 성공 시 실제 사용자 ID를 MDC에 저장한다. */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        final String token = getJwtFromRequest(request);

        if (StringUtils.hasText(token)) {
            final JwtValidationType validationResult = jwtUtil.validateJWT(token);
            switch (validationResult) {
                case VALID_ACCESS -> {
                    Long userId = jwtUtil.getUserIdFromJwt(token);
                    MDC.put(RequestLoggingFilter.USER_ID, userId.toString());
                    CustomAuthenticationToken customAuthenticationToken =
                            new CustomAuthenticationToken(userId, null, null);
                    customAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(customAuthenticationToken);
                }
                case EXPIRED_ACCESS -> {
                    writeError(response, CustomAuthError.ACCESS_TOKEN_EXPIRED);
                    return;
                }
                case VALID_REFRESH, EXPIRED_REFRESH -> {
                    writeError(response, CustomAuthError.WRONG_TOKEN_TYPE);
                    return;
                }
                case INVALID_TOKEN, INVALID_SIGNATURE, UNSUPPORTED_TOKEN, UNSUPPORTED_SUBJECT, EMPTY_TOKEN -> {
                    writeError(response, CustomAuthError.INVALID_TOKEN);
                    return;
                }
            }
        } else {
            SecurityContextHolder.getContext().setAuthentication(
                    new AnonymousAuthenticationToken(
                            "anonymous",
                            "anonymousUser",
                            AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"))
            );
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

    private void writeError(HttpServletResponse response, CustomAuthError error) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(error.getStatusCode().value());
        response.getWriter().write(
                objectMapper.writeValueAsString(new ErrorResult(error.getCode(), error.getDescription())));
    }
}
