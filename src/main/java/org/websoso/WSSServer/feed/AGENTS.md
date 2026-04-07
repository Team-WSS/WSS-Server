<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-04-03 | Updated: 2026-04-03 -->

# feed

## Purpose
소소피드(SNS 피드) 기능 모듈. 피드 작성·수정·삭제·조회, 댓글, 좋아요, 신고, 인기 피드 집계 기능을 포함하는 자체 완결형 모듈입니다.

## Subdirectories

| Directory | Purpose |
|-----------|---------|
| `controller/` | 피드 REST API 엔드포인트 (see `controller/AGENTS.md`) |
| `domain/` | 피드 관련 JPA 엔티티 (see `domain/AGENTS.md`) |
| `repository/` | 피드 JPA + QueryDSL 리포지터리 (see `repository/AGENTS.md`) |
| `service/` | 피드 도메인 서비스 (see `service/AGENTS.md`) |

## For AI Agents

### Working In This Directory
- 피드 관련 신규 기능은 이 모듈 내 해당 레이어에 추가합니다.
- QueryDSL 커스텀 리포지터리(`FeedCustomRepositoryImpl`)가 복잡한 피드 조회 쿼리를 담당합니다. 수정 빈도가 높은 파일이므로 주의합니다.
- `FeedCategory`는 최근 제거된 기능입니다 — 피드 조회 시 카테고리 필드 관련 수정 주의.

### Testing Requirements
테스트는 `src/test/java/org/websoso/WSSServer/feed/`에 작성합니다.

## Dependencies

### Internal
- `application/` — FeedFindApplication, FeedManagementApplication
- `user/` — 피드 작성자 유저 정보
- `notification/` — 좋아요·댓글 알림 발송

<!-- MANUAL: -->
