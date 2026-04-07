<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-04-03 | Updated: 2026-04-03 -->

# oauth2/domain

## Purpose
OAuth2 인증 관련 JPA 엔티티. JWT 리프레시 토큰 관리와 Apple 소셜 계정 연동 정보를 저장합니다.

## Key Files

| File | Description |
|------|-------------|
| `RefreshToken.java` | JWT 리프레시 토큰 엔티티 (유저-토큰 매핑) |
| `UserAppleToken.java` | Apple 소셜 로그인 토큰 정보 엔티티 |

## For AI Agents

### Working In This Directory
- `RefreshToken`은 토큰 재발급(`AuthApplication`) 시 검증에 사용됩니다.
- `UserAppleToken`은 Apple 계정 정보 동기화 및 회원 탈퇴 시 Apple 토큰 폐기에 사용됩니다.

<!-- MANUAL: -->
