<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-04-03 | Updated: 2026-04-03 -->

# feed/repository

## Purpose
피드 기능의 데이터 접근 레이어. 표준 JPA 리포지터리와 QueryDSL 커스텀 리포지터리를 함께 제공합니다. 복잡한 피드 조회 쿼리(차단 유저 필터링, 페이징, 인기 피드 집계)를 QueryDSL로 구현합니다.

## Key Files

| File | Description |
|------|-------------|
| `FeedRepository.java` | 피드 JPA 리포지터리 |
| `FeedCustomRepository.java` | 피드 커스텀 조회 인터페이스 |
| `FeedCustomRepositoryImpl.java` | QueryDSL 피드 커스텀 조회 구현체 **(핫스팟 - 가장 자주 수정됨)** |
| `CommentRepository.java` | 댓글 JPA 리포지터리 |
| `LikeRepository.java` | 좋아요 JPA 리포지터리 |
| `FeedImageRepository.java` | 피드 이미지 JPA 리포지터리 |
| `FeedImageCustomRepository.java` | 피드 이미지 커스텀 리포지터리 인터페이스 |
| `FeedCategoryRepository.java` | 피드 카테고리 JPA 리포지터리 |
| `FeedCategoryCustomRepository.java` | 피드 카테고리 커스텀 리포지터리 인터페이스 |
| `FeedCategoryCustomRepositoryImpl.java` | QueryDSL 피드 카테고리 구현체 |
| `PopularFeedRepository.java` | 인기 피드 JPA 리포지터리 |
| `PopularFeedCustomRepository.java` | 인기 피드 커스텀 리포지터리 인터페이스 |
| `PopularFeedCustomRepositoryImpl.java` | QueryDSL 인기 피드 구현체 |
| `ReportedFeedRepository.java` | 신고 피드 리포지터리 |
| `ReportedCommentRepository.java` | 신고 댓글 리포지터리 |

## For AI Agents

### Working In This Directory
- `FeedCustomRepositoryImpl`은 이 프로젝트에서 가장 빈번하게 수정되는 파일입니다. 변경 전 기존 쿼리 패턴을 반드시 확인하세요.
- 차단 유저 피드 필터링 로직이 이 파일에 집중되어 있습니다.
- QueryDSL Q클래스는 `./gradlew compileJava` 후 `build/generated/`에 생성됩니다.
- 새 커스텀 쿼리 추가 시: 인터페이스(`FooCustomRepository`) → 구현체(`FooCustomRepositoryImpl`) → JPA 리포지터리에 인터페이스 상속 순으로 진행합니다.

<!-- MANUAL: -->
