<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-04-03 | Updated: 2026-04-03 -->

# service

## Purpose
공통 서비스 클래스. 특정 기능 모듈에 속하지 않는 공유 서비스(이미지 업로드, SosoPick 조회)를 제공합니다.

## Key Files

| File | Description |
|------|-------------|
| `ImageClient.java` | AWS S3 이미지 업로드·삭제 클라이언트 서비스 |
| `SosoPickService.java` | 오늘의 발견(SosoPick) 조회 서비스 |

## For AI Agents

### Working In This Directory
- `ImageClient`는 S3 설정(`config/S3Config`)에 의존합니다.
- 이미지 업로드 관련 기능은 모두 `ImageClient`를 통해 처리합니다. 직접 S3 SDK를 호출하지 않습니다.

## Dependencies

### External
- AWS SDK S3 — 이미지 파일 스토리지

<!-- MANUAL: -->
