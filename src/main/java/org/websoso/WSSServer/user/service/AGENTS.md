<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-04-03 | Updated: 2026-04-03 -->

# user/service

## Purpose
유저 도메인 서비스. 유저 정보 조회·수정, 아바타 관리, 차단 기능의 비즈니스 로직을 담당합니다.

## Key Files

| File | Description |
|------|-------------|
| `UserService.java` | 유저 조회·수정·삭제 서비스 |
| `AvatarService.java` | 아바타 목록 조회·선택 서비스 |
| `BlockService.java` | 유저 차단·해제·차단 목록 조회 서비스 |

## For AI Agents

### Working In This Directory
- `UserService`는 대부분의 다른 Application 클래스에서도 유저 조회에 활용됩니다.
- FCM 토큰 저장/삭제는 `UserService` 내에서 처리합니다.

<!-- MANUAL: -->
