<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-04-03 | Updated: 2026-04-03 -->

# notification/repository

## Purpose
알림 데이터 접근 레이어. 알림 이력·유형·읽음 상태·디바이스 토큰의 JPA 리포지터리와 커스텀 QueryDSL 리포지터리를 제공합니다.

## Key Files

| File | Description |
|------|-------------|
| `NotificationRepository.java` | 알림 JPA 리포지터리 |
| `NotificationCustomRepository.java` | 알림 커스텀 조회 인터페이스 |
| `NotificationCustomRepositoryImpl.java` | QueryDSL 알림 커스텀 조회 구현체 |
| `NotificationTypeRepository.java` | 알림 유형 JPA 리포지터리 |
| `ReadNotificationRepository.java` | 읽음 상태 JPA 리포지터리 |
| `UserDeviceRepository.java` | FCM 디바이스 토큰 JPA 리포지터리 |

## For AI Agents

### Working In This Directory
- 페이징된 알림 목록 조회 등 복잡한 쿼리는 `NotificationCustomRepositoryImpl`에서 QueryDSL로 구현합니다.

<!-- MANUAL: -->
