<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-04-03 | Updated: 2026-04-03 -->

# repository

## Purpose
공통 JPA 리포지터리. 특정 기능 모듈에 속하지 않는 공유 엔티티(Genre, GenrePreference)의 데이터 접근 레이어입니다.

## Key Files

| File | Description |
|------|-------------|
| `GenreRepository.java` | 장르 엔티티 JPA 리포지터리 |
| `GenrePreferenceRepository.java` | 유저 장르 선호도 JPA 리포지터리 |

## For AI Agents

### Working In This Directory
- 피드·유저·알림 등 기능별 리포지터리는 각 모듈(`feed/repository/`, `user/repository/` 등)에 위치합니다.
- 여러 모듈에서 공유하는 엔티티의 리포지터리만 이 패키지에 배치합니다.

<!-- MANUAL: -->
