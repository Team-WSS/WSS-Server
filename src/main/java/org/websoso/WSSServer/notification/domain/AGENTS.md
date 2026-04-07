<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-04-03 | Updated: 2026-04-03 -->

# notification/domain

## Purpose
알림 기능의 JPA 엔티티. 알림 이력, 알림 유형, 읽음 상태, 유저 디바이스(FCM 토큰) 엔티티를 포함합니다.

## Key Files

| File | Description |
|------|-------------|
| `Notification.java` | 알림 이력 엔티티 (제목, 본문, 링크, 발신/수신자) |
| `NotificationType.java` | 알림 유형 엔티티 (COMMENT, LIKE, SYSTEM 등) |
| `ReadNotification.java` | 알림 읽음 상태 엔티티 |
| `UserDevice.java` | FCM 디바이스 토큰 엔티티 (유저-디바이스 매핑) |

## For AI Agents

### Working In This Directory
- 새 알림 유형 추가 시 `NotificationType` 열거형/엔티티와 `dto/`의 관련 DTO를 함께 수정합니다.
- `UserDevice`는 하나의 유저가 여러 디바이스를 가질 수 있는 구조입니다.

<!-- MANUAL: -->
