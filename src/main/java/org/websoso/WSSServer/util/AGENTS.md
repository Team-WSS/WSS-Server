<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-04-03 | Updated: 2026-04-03 -->

# util

## Purpose
애플리케이션 전반에서 사용하는 유틸리티 클래스 모음. 타입 변환, 정렬 기준 파싱, 시간 포맷 처리를 담당합니다.

## Key Files

| File | Description |
|------|-------------|
| `FeedGetOptionConverter.java` | 문자열 → `FeedGetOption` 열거형 변환기 |
| `SortCriteriaConverter.java` | 문자열 → 정렬 기준 열거형 변환기 |
| `TimeFormatUtil.java` | 날짜/시간 포맷 유틸리티 |

## For AI Agents

### Working In This Directory
- 순수 정적 유틸리티 메서드만 포함합니다. Spring 빈 의존성을 갖지 않도록 합니다.
- 테스트는 `src/test/java/org/websoso/WSSServer/util/`에 작성합니다.

<!-- MANUAL: -->
