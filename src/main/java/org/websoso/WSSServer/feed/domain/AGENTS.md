<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-04-03 | Updated: 2026-04-03 -->

# feed/domain

## Purpose
소소피드 기능의 JPA 엔티티. 피드 본문, 이미지, 카테고리, 좋아요, 댓글, 신고, 인기 피드 집계 엔티티를 포함합니다.

## Key Files

| File | Description |
|------|-------------|
| `Feed.java` | 피드 핵심 엔티티 (본문, 작성자, 소설 연관, 스포일러 여부 등) |
| `Comment.java` | 댓글 엔티티 |
| `FeedImage.java` | 피드 이미지 엔티티 |
| `FeedCategory.java` | 피드 카테고리 엔티티 (현재 제거 진행 중) |
| `Category.java` | 카테고리 열거형·엔티티 |
| `Like.java` | 좋아요 엔티티 (유저-피드 N:M) |
| `PopularFeed.java` | 인기 피드 집계 엔티티 |
| `ReportedFeed.java` | 신고된 피드 엔티티 |
| `ReportedComment.java` | 신고된 댓글 엔티티 |

## For AI Agents

### Working In This Directory
- `FeedCategory`는 최근 피드 조회에서 제거된 기능입니다. 관련 필드나 조인을 새로 추가하지 않습니다.
- `Feed` 엔티티 수정 시 `FeedCustomRepositoryImpl`의 QueryDSL 쿼리에 미치는 영향을 확인합니다.

<!-- MANUAL: -->
