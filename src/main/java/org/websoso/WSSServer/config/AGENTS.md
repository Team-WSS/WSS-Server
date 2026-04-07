<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-04-03 | Updated: 2026-04-03 -->

# config

## Purpose
Spring 애플리케이션 설정 클래스 모음. Security, QueryDSL, S3, Web MVC, JPA Auditing 등 인프라 수준의 설정을 담당합니다.

## Key Files

| File | Description |
|------|-------------|
| `JpaAuditingConfig.java` | `@EnableJpaAuditing` 설정 (BaseEntity 생성/수정 시각 자동화) |
| `QueryDslConfig.java` | `JPAQueryFactory` 빈 등록 |
| `S3Config.java` | AWS S3 클라이언트 빈 설정 |
| `SecurityConfig.java` | Spring Security 필터 체인, 공개/보호 엔드포인트, CORS 설정 |
| `WebConfig.java` | `CustomUserArgumentResolver` 등록, CORS 등 MVC 설정 |

## Subdirectories

| Directory | Purpose |
|-----------|---------|
| `jwt/` | JWT 필터·프로바이더·유틸 클래스 (see `jwt/AGENTS.md`) |

## For AI Agents

### Working In This Directory
- 새 엔드포인트를 비인증 허용으로 열 때 `SecurityConfig`의 `permitAll()` 목록을 수정합니다.
- QueryDSL Q클래스 생성은 `./gradlew compileJava` 실행 후 `build/generated/` 경로에 생성됩니다.
- S3 관련 설정은 `config-repo/`의 YAML에서 주입됩니다. 시크릿을 소스에 하드코딩하지 않습니다.

## Dependencies

### External
- Spring Security — 인증·인가 필터 체인
- QueryDSL — JPAQueryFactory
- AWS SDK — S3 클라이언트

<!-- MANUAL: -->
