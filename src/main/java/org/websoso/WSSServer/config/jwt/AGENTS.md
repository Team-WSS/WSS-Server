<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-04-03 | Updated: 2026-04-03 -->

# config/jwt

## Purpose
JWT 인증 인프라. 토큰 생성·파싱·검증, Spring Security 필터 통합, 인증 실패 핸들러를 포함합니다.

## Key Files

| File | Description |
|------|-------------|
| `JwtProvider.java` | JWT 토큰 생성(액세스·리프레시) 및 클레임 추출 |
| `JWTUtil.java` | JWT 유틸리티 (토큰에서 유저 ID 추출 등) |
| `JwtAuthenticationFilter.java` | Spring Security 필터: 요청 헤더에서 JWT를 추출하고 `SecurityContext` 설정 |
| `JwtValidationType.java` | JWT 검증 결과 열거형 (VALID, EXPIRED, INVALID 등) |
| `CustomAuthenticationToken.java` | 인증된 유저 정보를 담는 커스텀 `Authentication` 객체 |
| `CustomAccessDeniedHandler.java` | 인가 실패(403) 응답 핸들러 |
| `CustomJwtAuthenticationEntryPoint.java` | 인증 실패(401) 응답 핸들러 |

## For AI Agents

### Working In This Directory
- JWT 시크릿 키는 `config-repo/`의 YAML에서 주입됩니다. 소스에 하드코딩하지 않습니다.
- 토큰 만료 시간 변경은 `JwtProvider`의 설정값을 수정합니다.
- `JwtAuthenticationFilter`는 `SecurityConfig`에서 필터 체인에 등록됩니다.

## Dependencies

### External
- JJWT 0.11.5 — JWT 생성 및 파싱

<!-- MANUAL: -->
