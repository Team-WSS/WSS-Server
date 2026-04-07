<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-04-03 | Updated: 2026-04-03 -->

# auth

## Purpose
JWT 기반 인증·인가 처리. 요청에서 JWT를 추출·검증하고, 컨트롤러 메서드 파라미터에 현재 유저 정보를 주입합니다. 리소스별 접근 권한 검증도 담당합니다.

## Key Files

| File | Description |
|------|-------------|
| `AuthorizationService.java` | 인가 서비스: 리소스 소유자 여부 등 접근 권한 검증 |
| `CustomUserArgumentResolver.java` | `@CurrentUser` 어노테이션으로 컨트롤러 파라미터에 유저 ID 주입 |
| `ResourceAuthorizationHandler.java` | 리소스 유형별 인가 핸들러 라우터 |

## Subdirectories

| Directory | Purpose |
|-----------|---------|
| `validator/` | 리소스별 인가 Validator 구현체 (see `validator/AGENTS.md`) |

## For AI Agents

### Working In This Directory
- JWT 필터(`JwtAuthenticationFilter`)는 `config/jwt/`에 위치합니다.
- 새 리소스에 대한 접근 제어가 필요하면 `validator/`에 `ResourceAuthorizationValidator` 구현체를 추가하고 `ResourceAuthorizationHandler`에 등록합니다.
- `CustomUserArgumentResolver`는 `config/WebConfig`에 등록되어 있습니다.

### Common Patterns
- 컨트롤러에서 `@CurrentUser Long userId`로 인증된 유저 ID를 받습니다.
- 비로그인 허용 엔드포인트는 `config/SecurityConfig`의 `permitAll()` 목록에 추가합니다.

## Dependencies

### Internal
- `config/jwt/` — JWT 토큰 파싱 및 검증
- `user/` — 유저 존재 여부 확인

<!-- MANUAL: -->
