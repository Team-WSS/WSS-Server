<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-04-03 | Updated: 2026-04-03 -->

# notification

## Purpose
푸시 알림 기능 모듈. Firebase Cloud Messaging(FCM)을 통한 알림 발송, 알림 이력 저장·조회, 읽음 처리를 담당합니다.

## Key Files

| File | Description |
|------|-------------|
| `FCMClient.java` | Firebase Cloud Messaging 클라이언트 래퍼 |
| `FCMConfig.java` | FCM SDK 설정 및 빈 등록 |

## Subdirectories

| Directory | Purpose |
|-----------|---------|
| `controller/` | 알림 조회·읽음 처리 API 엔드포인트 |
| `controller/response/` | 알림 응답 DTO |
| `domain/` | 알림 JPA 엔티티 |
| `dto/` | 알림 발송용 DTO |
| `repository/` | 알림 리포지터리 |
| `service/` | 알림 도메인 서비스 + FCM 발송 서비스 |

## For AI Agents

### Working In This Directory
- FCM 자격증명 파일은 `config-repo/static/fcm/`에 위치하며, 소스에 직접 포함하지 않습니다.
- `NotificationSendApplication`이 알림 발송 오케스트레이션을 담당합니다.
- 새 알림 유형 추가 시 `domain/`의 알림 유형 열거형을 함께 확장합니다.

## Dependencies

### Internal
- `application/` — NotificationApplication, NotificationSendApplication
- `user/` — FCM 토큰 조회

### External
- Firebase Admin SDK — 푸시 알림 발송

<!-- MANUAL: -->
