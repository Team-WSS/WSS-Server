<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-04-03 | Updated: 2026-04-03 -->

# oauth2/service

## Purpose
Apple·Kakao OAuth2 인증 서비스. 각 플랫폼의 토큰 검증, 유저 정보 조회, 신규 계정 생성, 내부 JWT 발급을 처리합니다.

## For AI Agents

### Working In This Directory
- Apple 로그인: identity token(JWT)을 Apple JWKS로 검증 후 sub 클레임으로 계정 식별.
- Kakao 로그인: 액세스 토큰으로 Kakao API 호출 후 계정 정보 조회.
- 두 플랫폼 모두 최초 로그인 시 `User` 엔티티를 생성합니다.
- Apple 계정 정보 동기화 API도 이 서비스에서 처리합니다.

<!-- MANUAL: -->
