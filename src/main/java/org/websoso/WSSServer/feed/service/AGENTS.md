<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-04-03 | Updated: 2026-04-03 -->

# feed/service

## Purpose
피드 도메인 서비스. 피드·댓글 CRUD, 좋아요 토글, 신고, 인기 피드 서비스를 인터페이스와 구현체 쌍으로 제공합니다.

## Key Files

| File | Description |
|------|-------------|
| `FeedService.java` | 피드 서비스 인터페이스 |
| `FeedServiceImpl.java` | 피드 서비스 구현체 |
| `CommentService.java` | 댓글 서비스 인터페이스 |
| `CommentServiceImpl.java` | 댓글 서비스 구현체 |
| `PopularFeedService.java` | 인기 피드 집계 서비스 |
| `ReportService.java` | 신고 서비스 인터페이스 |
| `ReportServiceImpl.java` | 신고 서비스 구현체 |

## For AI Agents

### Working In This Directory
- 서비스 구현체는 `@Service`로 등록되며 `application/` 레이어에서 호출됩니다.
- 인터페이스(`FooService`) + 구현체(`FooServiceImpl`) 쌍 패턴을 유지합니다.
- 트랜잭션은 `application/` 레이어에서 관리합니다. 서비스 메서드에 `@Transactional`을 중복 선언하지 않습니다.

<!-- MANUAL: -->
