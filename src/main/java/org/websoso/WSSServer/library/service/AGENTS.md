<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-04-03 | Updated: 2026-04-03 -->

# library/service

## Purpose
유저 서재 도메인 서비스. 서재 조회·등록·수정, 키워드·매력포인트 관리, 작품 평가, 관심 표시 로직을 담당합니다.

## Key Files

| File | Description |
|------|-------------|
| `LibraryService.java` | 서재 조회·등록·수정 서비스 |
| `UserNovelService.java` | UserNovel 생성·수정·삭제 서비스 |
| `KeywordService.java` | 키워드 조회 서비스 |
| `AttractivePointService.java` | 매력포인트 조회 서비스 |

## For AI Agents

### Working In This Directory
- `LibraryEvaluationApplication`과 `LibraryInterestApplication`이 이 서비스들을 오케스트레이션합니다.
- `UserNovel` 리팩터링 계획이 진행 중이므로, 기존 서비스 로직 수정 전 `user-novels-refactor-plan.md`를 확인하세요.

<!-- MANUAL: -->
