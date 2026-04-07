<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-04-03 | Updated: 2026-04-03 -->

# library

## Purpose
유저 서재(독서 기록) 기능 모듈. 유저가 소설을 서재에 등록하고 평가(별점·키워드·매력포인트)하거나 관심 표시하는 기능을 담당합니다.

## Subdirectories

| Directory | Purpose |
|-----------|---------|
| `domain/` | 서재 관련 JPA 엔티티 (UserNovel, 평가 등) |
| `repository/` | 서재 JPA + QueryDSL 리포지터리 |
| `service/` | 서재 도메인 서비스 |

## For AI Agents

### Working In This Directory
- `LibraryEvaluationApplication`(작품 평가)과 `LibraryInterestApplication`(관심 표시)에서 이 모듈의 서비스를 호출합니다.
- `UserNovel` 리팩터링 계획이 진행 중입니다 (`user-novels-refactor-plan.md` 참고).

### Testing Requirements
테스트는 `src/test/java/org/websoso/WSSServer/library/`에 작성합니다.

## Dependencies

### Internal
- `user/` — 유저 정보
- `novel/` — 소설 정보
- `domain/` — Genre, GenrePreference

<!-- MANUAL: -->
