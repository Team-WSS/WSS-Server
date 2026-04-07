<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-04-03 | Updated: 2026-04-03 -->

# oauth2/repository

## Purpose
OAuth2 인증 데이터 접근 레이어. 리프레시 토큰과 Apple 소셜 계정 토큰의 JPA 리포지터리를 제공합니다.

## Key Files

| File | Description |
|------|-------------|
| `RefreshTokenRepository.java` | 리프레시 토큰 JPA 리포지터리 |
| `UserAppleTokenRepository.java` | Apple 소셜 계정 토큰 JPA 리포지터리 |

## For AI Agents

### Working In This Directory
- 토큰 재발급 시 `RefreshTokenRepository`로 기존 토큰 유효성을 검증합니다.
- 회원 탈퇴 시 `UserAppleTokenRepository`에서 Apple 토큰을 조회하여 Apple 서버에 폐기 요청을 보냅니다.

<!-- MANUAL: -->
