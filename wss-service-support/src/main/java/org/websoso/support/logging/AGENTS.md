<!-- Parent: ../../../../../../AGENTS.md -->
<!-- Generated: 2026-04-03 | Updated: 2026-04-03 -->

# wss-service-support/logging

## Purpose
HTTP 요청 및 SQL 쿼리 로깅 지원 기능. 요청 본문 로깅 AOP 어드바이스, 요청 로깅 필터, P6Spy SQL 포매터를 제공합니다.

## Subdirectories

| Directory | Purpose |
|-----------|---------|
| `request/` | HTTP 요청 로깅 필터 및 AOP 어드바이스 |
| `sql/` | P6Spy 기반 SQL 쿼리 로깅 포매터 |

## For AI Agents

### Working In This Directory
- 로깅 설정은 `config-repo/`의 YAML에서 활성화·비활성화를 제어합니다.
- SQL 로깅은 P6Spy를 통해 실제 파라미터가 바인딩된 쿼리를 출력합니다 (운영 환경에서는 비활성화 권장).

<!-- MANUAL: -->
