<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-04-03 | Updated: 2026-04-03 -->

# oauth2

## Purpose
Apple·Kakao 소셜 로그인(OAuth2) 기능 모듈. 각 플랫폼의 토큰 검증, 유저 정보 조회, 최초 로그인 시 계정 생성, JWT 발급을 담당합니다.

## Subdirectories

| Directory | Purpose |
|-----------|---------|
| `controller/` | 로그인 API 엔드포인트 (Apple, Kakao) |
| `domain/` | OAuth2 관련 엔티티 (플랫폼 계정 연동 정보 등) |
| `dto/` | 로그인 요청·응답 DTO |
| `repository/` | OAuth2 계정 리포지터리 |
| `service/` | Apple·Kakao 토큰 검증 및 계정 처리 서비스 |

## For AI Agents

### Working In This Directory
- Apple 관련 설정 파일은 `config-repo/static/apple/`에 위치합니다. 소스에 직접 포함하지 않습니다.
- Apple 로그인은 JWT(identity token) 검증 방식, Kakao는 액세스 토큰으로 사용자 정보를 조회합니다.
- 로그인 성공 시 내부 JWT를 발급하여 반환합니다 (`config/jwt/JwtProvider` 사용).

## Dependencies

### Internal
- `config/jwt/` — JWT 토큰 발급
- `user/` — 유저 계정 생성·조회

### External
- Apple Sign In (JWKS 기반 토큰 검증)
- Kakao OAuth2 API

<!-- MANUAL: -->
