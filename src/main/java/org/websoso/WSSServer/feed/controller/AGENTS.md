<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-04-03 | Updated: 2026-04-03 -->

# feed/controller

## Purpose
소소피드 REST API 엔드포인트. 피드 전체 조회, 상세 조회, 작성·수정·삭제, 좋아요, 댓글 CRUD, 인기 피드, 신고 API를 제공합니다.

## For AI Agents

### Working In This Directory
- 컨트롤러는 요청 파싱과 `application/` 레이어 호출만 담당합니다.
- 피드 관련 인가 검증은 `auth/validator/FeedAuthorizationValidator`에서 처리됩니다.
- `@CurrentUser Long userId`로 인증된 유저 ID를 주입받습니다.

<!-- MANUAL: -->
