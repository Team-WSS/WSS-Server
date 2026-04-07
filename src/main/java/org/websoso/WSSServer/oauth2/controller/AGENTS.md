<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-04-03 | Updated: 2026-04-03 -->

# oauth2/controller

## Purpose
소셜 로그인(Apple·Kakao) REST API 엔드포인트. OAuth2 로그인, 토큰 재발급, Apple 계정 정보 동기화 API를 제공합니다.

## Key Files

| File | Description |
|------|-------------|
| `AuthController.java` | Apple·Kakao 로그인, 토큰 재발급, Apple 계정 동기화, 로그아웃 API |

## For AI Agents

### Working In This Directory
- 로그인 엔드포인트는 `SecurityConfig`에서 `permitAll()`로 설정되어 있습니다 (인증 불필요).
- 응답 DTO는 `oauth2/dto/`에 위치합니다.

<!-- MANUAL: -->
