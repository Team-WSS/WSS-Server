<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-04-03 | Updated: 2026-04-03 -->

# library/repository

## Purpose
유저 서재 데이터 접근 레이어. UserNovel, 키워드, 매력포인트의 JPA 리포지터리와 QueryDSL 커스텀 리포지터리를 제공합니다.

## Key Files

| File | Description |
|------|-------------|
| `UserNovelRepository.java` | UserNovel JPA 리포지터리 |
| `UserNovelCustomRepository.java` | UserNovel 커스텀 조회 인터페이스 |
| `UserNovelCustomRepositoryImpl.java` | QueryDSL UserNovel 커스텀 조회 구현체 |
| `KeywordRepository.java` | 키워드 JPA 리포지터리 |
| `KeywordCategoryRepository.java` | 키워드 카테고리 JPA 리포지터리 |
| `AttractivePointRepository.java` | 매력포인트 JPA 리포지터리 |
| `UserNovelKeywordRepository.java` | UserNovel-Keyword N:M 리포지터리 |
| `UserNovelAttractivePointRepository.java` | UserNovel-AttractivePoint N:M 리포지터리 |

## For AI Agents

### Working In This Directory
- 서재 조회(읽기 상태별 필터, 정렬 등) 복잡한 쿼리는 `UserNovelCustomRepositoryImpl`에서 QueryDSL로 구현합니다.

<!-- MANUAL: -->
