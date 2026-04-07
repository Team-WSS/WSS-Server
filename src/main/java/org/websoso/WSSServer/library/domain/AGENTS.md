<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-04-03 | Updated: 2026-04-03 -->

# library/domain

## Purpose
유저 서재(독서 기록) 기능의 JPA 엔티티. 유저-소설 관계(`UserNovel`), 키워드·매력포인트 태깅, 부모 터치 리스너 등을 모델링합니다.

## Key Files

| File | Description |
|------|-------------|
| `UserNovel.java` | 유저-소설 관계 핵심 엔티티 (읽기 상태, 별점, 날짜 등) |
| `Keyword.java` | 소설 키워드 엔티티 |
| `KeywordCategory.java` | 키워드 카테고리 엔티티 |
| `AttractivePoint.java` | 소설 매력포인트 엔티티 |
| `UserNovelKeyword.java` | UserNovel-Keyword N:M 연결 엔티티 |
| `UserNovelAttractivePoint.java` | UserNovel-AttractivePoint N:M 연결 엔티티 |
| `ParentTouchListener.java` | JPA 엔티티 라이프사이클 리스너 |

## For AI Agents

### Working In This Directory
- `UserNovel` 리팩터링이 진행 중입니다 (`user-novels-refactor-plan.md` 참고). 이 엔티티 수정 전 계획 문서를 먼저 확인하세요.
- `UserNovelKeyword`, `UserNovelAttractivePoint`는 복합키 구조를 사용합니다.

<!-- MANUAL: -->
