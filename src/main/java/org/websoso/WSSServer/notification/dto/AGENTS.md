<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-04-03 | Updated: 2026-04-03 -->

# notification/dto

## Purpose
알림 발송 및 설정 관련 DTO. FCM 메시지 발송 요청, 토큰 저장, 알림 생성 요청, 푸시 설정 응답 등을 포함합니다.

## Key Files

| File | Description |
|------|-------------|
| `FCMMessageRequest.java` | FCM 푸시 메시지 발송 요청 DTO |
| `FCMTokenRequest.java` | FCM 토큰 저장 요청 DTO |
| `NotificationCreateRequest.java` | 알림 생성 요청 DTO |
| `PushSettingGetResponse.java` | 푸시 알림 설정 조회 응답 DTO |
| `PushSettingRequest.java` | 푸시 알림 설정 변경 요청 DTO |
| `ReadNotificationDto.java` | 알림 읽음 처리 DTO |

## For AI Agents

### Working In This Directory
- 발송용 DTO(`FCMMessageRequest`)와 API 응답용 DTO를 명확히 구분합니다.
- 새 알림 유형 추가 시 이 패키지의 관련 DTO를 함께 확장합니다.

<!-- MANUAL: -->
