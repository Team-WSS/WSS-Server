<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-04-03 | Updated: 2026-04-03 -->

# exception

## Purpose
도메인별 커스텀 예외·에러 코드 및 전역 예외 핸들러. 각 도메인마다 `Custom<Domain>Error` enum과 `Custom<Domain>Exception` 클래스 쌍으로 구성하는 패턴을 따릅니다.

## Key Files

| File | Description |
|------|-------------|
| `Custom*Error.java` (20개) | 도메인별 에러 코드 열거형 (HTTP 상태 코드 + 메시지 포함) |
| `Custom*Exception.java` (20개) | 도메인별 커스텀 런타임 예외 클래스 |
| `GlobalExceptionHandler.java` | `@RestControllerAdvice` 전역 예외 핸들러 |

## Subdirectories

| Directory | Purpose |
|-----------|---------|
| `error/` | 에러 코드 열거형 (현재는 루트에 위치) |
| `exception/` | 예외 클래스 (현재는 루트에 위치) |
| `handler/` | 전역 예외 핸들러 (현재는 루트에 위치) |

## For AI Agents

### Working In This Directory
- 새 도메인 예외 추가 시 `Custom<Domain>Error.java`와 `Custom<Domain>Exception.java`를 함께 생성합니다.
- `GlobalExceptionHandler`에 새 예외 타입에 대한 `@ExceptionHandler`를 추가합니다.
- 에러 응답 형식은 기존 `GlobalExceptionHandler` 패턴을 따릅니다.

### Common Patterns
```java
// Error enum 예시
public enum CustomFeedError {
    FEED_NOT_FOUND(HttpStatus.NOT_FOUND, "피드를 찾을 수 없습니다.");
    // ...
}

// Exception 클래스 예시
public class CustomFeedException extends RuntimeException {
    private final CustomFeedError error;
    public CustomFeedException(CustomFeedError error) {
        super(error.getMessage());
        this.error = error;
    }
}
```

## Dependencies

### Internal
- 모든 도메인 레이어에서 사용

<!-- MANUAL: -->
