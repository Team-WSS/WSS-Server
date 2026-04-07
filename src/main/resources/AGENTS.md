<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-04-03 | Updated: 2026-04-03 -->

# src/main/resources

## Purpose
Spring Boot 런타임 설정 파일 위치. `application.yml`이 로컬 프로파일을 로드하고, 외부 `config-repo/`를 import하여 환경별 설정을 분리합니다.

## Key Files

| File | Description |
|------|-------------|
| `application.yml` | Spring Boot 기본 설정. `config-repo/`의 `application-common.yml` 등을 import |

## For AI Agents

### Working In This Directory
- 시크릿(DB 비밀번호, API 키, private key 등)을 이 파일에 직접 커밋하지 않습니다.
- 환경별 설정은 `config-repo/` 하위 YAML 파일에 분리 관리합니다.
- 로컬 실행 시 `config-repo/` 디렉터리가 반드시 존재해야 합니다.

<!-- MANUAL: -->
