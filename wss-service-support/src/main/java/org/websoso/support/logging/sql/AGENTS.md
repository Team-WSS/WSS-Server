<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-04-03 | Updated: 2026-04-03 -->

# logging/sql

## Purpose
P6Spy 기반 SQL 쿼리 로깅 포매터. JPA/Hibernate가 실행하는 SQL 쿼리에 실제 파라미터 값을 바인딩하여 읽기 쉬운 형태로 출력합니다.

## Key Files

| File | Description |
|------|-------------|
| `P6SpySqlFormatter.java` | P6Spy `MessageFormattingStrategy` 구현체. SQL에 파라미터 값을 인라인으로 포매팅 |

## For AI Agents

### Working In This Directory
- P6Spy 설정은 `spy.properties`(resources)에서 이 포매터를 참조합니다.
- 운영 환경에서는 SQL 로깅을 비활성화하여 성능 영향을 방지합니다.

<!-- MANUAL: -->
