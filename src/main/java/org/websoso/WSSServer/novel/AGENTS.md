<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-04-03 | Updated: 2026-04-03 -->

# novel

## Purpose
소설 작품 검색·조회 기능 모듈. 키워드 검색, 상세 탐색(필터), 소설 상세 정보 조회, 인기 소설 집계를 담당합니다.

## Subdirectories

| Directory | Purpose |
|-----------|---------|
| `domain/` | 소설 관련 JPA 엔티티 (Novel, Keyword 등) |
| `repository/` | 소설 JPA + QueryDSL 리포지터리 |
| `service/` | 소설 도메인 서비스 |

## For AI Agents

### Working In This Directory
- `SearchNovelApplication`이 이 모듈의 서비스를 호출하는 검색 유스케이스를 처리합니다.
- `PopularNovelScheduler`가 주기적으로 인기 소설을 집계합니다.
- `PopularNovelGetResponse`는 자주 수정되는 파일입니다 (`dto/popularNovel/`에 위치).

## Dependencies

### Internal
- `library/` — 유저별 소설 평가·서재 정보
- `domain/` — Genre

<!-- MANUAL: -->
