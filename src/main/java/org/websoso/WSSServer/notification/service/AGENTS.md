<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-04-03 | Updated: 2026-04-03 -->

# notification/service

## Purpose
알림 도메인 서비스. 알림 이력 저장·조회·읽음 처리와 FCM을 통한 실제 푸시 발송을 담당합니다.

## For AI Agents

### Working In This Directory
- FCM 발송은 `FCMClient`를 통해 수행합니다. 직접 Firebase SDK를 호출하지 않습니다.
- 알림 발송 실패 시 예외를 전파하지 않고 로깅만 하는 패턴을 유지합니다 (알림 실패가 핵심 기능을 방해하지 않도록).
- 새 알림 유형 추가 시 `domain/`의 알림 유형 열거형과 `dto/`의 알림 DTO를 함께 확장합니다.

<!-- MANUAL: -->
