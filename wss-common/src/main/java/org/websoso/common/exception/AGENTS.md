<!-- Parent: ../../../../../../AGENTS.md -->
<!-- Generated: 2026-04-03 | Updated: 2026-04-03 -->

# wss-common/exception

## Purpose
공통 예외 계약(인터페이스·추상 클래스). 도메인별 커스텀 예외가 준수해야 할 인터페이스(`ICustomError`)와 공통 예외 추상 클래스(`AbstractCustomException`), 에러 응답 형식(`ErrorResult`)을 정의합니다.

## Key Files

| File | Description |
|------|-------------|
| `ICustomError.java` | 모든 커스텀 에러 열거형이 구현하는 인터페이스 (HTTP 상태, 메시지 계약) |
| `AbstractCustomException.java` | 모든 커스텀 예외가 상속하는 추상 기반 예외 클래스 |
| `ErrorResult.java` | API 에러 응답 공통 형식 DTO |

## For AI Agents

### Working In This Directory
- 메인 모듈의 `Custom*Error` 열거형은 `ICustomError`를 구현하고, `Custom*Exception`은 `AbstractCustomException`을 상속합니다.
- 이 인터페이스/추상 클래스의 계약을 변경하면 모든 도메인 예외에 영향을 줍니다.

<!-- MANUAL: -->
