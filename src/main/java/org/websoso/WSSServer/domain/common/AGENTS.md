<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-04-03 | Updated: 2026-04-03 -->

# domain/common

## Purpose
여러 도메인에서 공유하는 열거형(enum) 및 도메인 유틸리티 클래스. 카테고리명, 매력포인트, 피드 조회 옵션, Discord 알림 메시지 템플릿 등을 포함합니다.

## Key Files

| File | Description |
|------|-------------|
| `Action.java` | 좋아요·관심 등 사용자 액션 열거형 |
| `AttractivePointName.java` | 작품 매력포인트 열거형 |
| `CategoryName.java` | 소설 카테고리명 열거형 |
| `FeedGetOption.java` | 피드 조회 옵션 열거형 (전체·팔로잉 등) |
| `DiscordMessageTemplate.java` | Discord 웹훅 메시지 템플릿 |
| `DiscordWebhookMessage.java` | Discord 웹훅 메시지 DTO |

## For AI Agents

### Working In This Directory
- 새 열거형이 여러 모듈에서 공유된다면 이 패키지에 배치합니다.
- 단일 모듈에서만 사용하는 열거형은 해당 모듈의 `domain/` 패키지에 배치합니다.

<!-- MANUAL: -->
