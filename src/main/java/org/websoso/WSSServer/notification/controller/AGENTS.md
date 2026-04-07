<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-04-03 | Updated: 2026-04-03 -->

# notification/controller

## Purpose
알림 및 디바이스 관리 REST API 엔드포인트. 알림 목록 조회·읽음 처리와 FCM 디바이스 토큰 관리 API를 제공합니다.

## Key Files

| File | Description |
|------|-------------|
| `NotificationController.java` | 알림 조회·읽음 처리 API |
| `UserDeviceController.java` | FCM 디바이스 토큰 저장·삭제 API |

## Subdirectories

| Directory | Purpose |
|-----------|---------|
| `response/` | 알림 조회 응답 DTO |

## For AI Agents

### Working In This Directory
- `@CurrentUser Long userId`로 인증된 유저 ID를 주입받습니다.
- 알림 소유자 인가 검증은 `auth/validator/NotificationAuthorizationValidator`에서 처리됩니다.

<!-- MANUAL: -->
