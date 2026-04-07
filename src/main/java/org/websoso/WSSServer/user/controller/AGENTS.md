<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-04-03 | Updated: 2026-04-03 -->

# user/controller

## Purpose
유저 계정·프로필·아바타·차단 REST API 엔드포인트. 프로필 조회·수정, 아바타 목록·선택, 차단 등록·해제·목록 조회 API를 제공합니다.

## Key Files

| File | Description |
|------|-------------|
| `UserController.java` | 유저 프로필 조회·수정, 닉네임 검증, 취향 설정, 탈퇴 API |
| `AvatarController.java` | 아바타 프로필 목록 조회·선택 API |
| `BlockController.java` | 유저 차단 등록·해제·차단 목록 조회 API |

## For AI Agents

### Working In This Directory
- `@CurrentUser Long userId`로 인증된 유저 ID를 주입받습니다.
- 차단 관련 인가는 `auth/validator/BlockAuthorizationValidator`에서 처리됩니다.
- 응답 DTO는 `dto/user/`, `dto/avatar/`, `dto/block/`에 위치합니다.

<!-- MANUAL: -->
