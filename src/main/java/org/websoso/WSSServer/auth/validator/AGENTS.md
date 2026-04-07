<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-04-03 | Updated: 2026-04-03 -->

# auth/validator

## Purpose
리소스 유형별 접근 권한 검증 구현체. `ResourceAuthorizationValidator` 인터페이스를 구현하여 각 도메인 리소스(피드, 댓글, 유저, 알림 등)의 소유자 여부를 확인합니다.

## Key Files

| File | Description |
|------|-------------|
| `ResourceAuthorizationValidator.java` | 인가 검증기 인터페이스 |
| `FeedAuthorizationValidator.java` | 피드 소유자 검증 |
| `FeedAccessValidator.java` | 피드 접근 가능 여부 검증 (차단 관계 포함) |
| `CommentAuthorizationValidator.java` | 댓글 소유자 검증 |
| `BlockAuthorizationValidator.java` | 차단 관계 검증 |
| `NotificationAuthorizationValidator.java` | 알림 소유자 검증 |
| `NovelAuthorizationValidator.java` | 소설 관련 접근 검증 |
| `UserNovelAuthorizationValidator.java` | 유저-소설 관계 접근 검증 |

## For AI Agents

### Working In This Directory
- 새 리소스 인가 규칙 추가 시: `ResourceAuthorizationValidator` 구현체 → `ResourceAuthorizationHandler`에 등록 순으로 진행합니다.
- 검증기는 Spring 빈으로 등록되며 `ResourceAuthorizationHandler`가 리소스 유형에 따라 적절한 검증기를 선택합니다.

<!-- MANUAL: -->
