<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-04-03 | Updated: 2026-04-03 -->

# logging/request

## Purpose
HTTP 요청 로깅 구현체. 요청 본문을 AOP 어드바이스로 로깅하고, 서블릿 필터로 요청 메타데이터(URI, 메서드, IP)를 기록합니다.

## Key Files

| File | Description |
|------|-------------|
| `RequestBodyLoggingAdvice.java` | `@ControllerAdvice`로 요청 본문을 읽어 로깅하는 AOP 어드바이스 |
| `RequestLoggingFilter.java` | 서블릿 필터로 요청 URI·메서드·IP 로깅 |

## For AI Agents

### Working In This Directory
- `RequestBodyLoggingAdvice`는 `HttpMessageConverter` 체인에 개입하므로 수정 시 성능 영향을 고려합니다.
- 민감 정보(비밀번호, 토큰) 필드는 로깅에서 마스킹합니다.

<!-- MANUAL: -->
