<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-04-03 | Updated: 2026-04-03 -->

# novel/service

## Purpose
소설 도메인 서비스. 소설 검색·상세 조회, 장르 정보 조회, 키워드 검색, 인기 소설 집계를 담당합니다.

## Key Files

| File | Description |
|------|-------------|
| `NovelServiceImpl.java` | 소설 조회·검색 서비스 구현체 |
| `GenreServiceImpl.java` | 장르 정보 조회 서비스 구현체 |
| `KeywordServiceImpl.java` | 키워드 기반 소설 검색 서비스 구현체 |
| `PopularNovelService.java` | 인기 소설 집계·조회 서비스 |

## For AI Agents

### Working In This Directory
- `SearchNovelApplication`이 이 서비스들을 조합하여 검색 유스케이스를 완성합니다.
- `PopularNovelService`는 `PopularNovelScheduler`와 함께 동작합니다.
- `PopularNovelGetResponse` DTO(`dto/popularNovel/`)가 이 서비스의 주요 출력 형태입니다 — 자주 수정되는 파일입니다.

<!-- MANUAL: -->
