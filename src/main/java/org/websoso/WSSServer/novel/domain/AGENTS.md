<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-04-03 | Updated: 2026-04-03 -->

# novel/domain

## Purpose
소설 작품 관련 JPA 엔티티. 소설 기본 정보, 장르 매핑, 연재 플랫폼, 인기 소설 집계 엔티티를 포함합니다.

## Key Files

| File | Description |
|------|-------------|
| `Novel.java` | 소설 핵심 엔티티 (제목, 작가, 표지, 소개, 연재 상태 등) |
| `NovelGenre.java` | 소설-장르 N:M 연결 엔티티 |
| `NovelPlatform.java` | 소설-플랫폼 N:M 연결 엔티티 |
| `Platform.java` | 연재 플랫폼 엔티티 (카카오페이지, 네이버시리즈 등) |
| `PopularNovel.java` | 인기 소설 집계 엔티티 |

## For AI Agents

### Working In This Directory
- `Novel`은 `UserNovel`(서재), `Feed`(피드 연관 소설) 등 여러 엔티티에서 참조합니다. 필드 변경 시 광범위한 영향을 검토합니다.
- `PopularNovel`은 `PopularNovelScheduler`가 주기적으로 집계하여 갱신합니다.

<!-- MANUAL: -->
