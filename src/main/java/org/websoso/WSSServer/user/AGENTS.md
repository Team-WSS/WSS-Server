<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-04-03 | Updated: 2026-04-03 -->

# user

## Purpose
유저 계정·프로필·아바타·차단 기능 모듈. 유저 정보 조회·수정, 아바타 선택, 닉네임 검증, 유저 간 차단 관계 관리를 담당합니다.

## Subdirectories

| Directory | Purpose |
|-----------|---------|
| `controller/` | 유저 프로필·아바타·차단 API 엔드포인트 |
| `domain/` | 유저 관련 JPA 엔티티 (User, Avatar, Block 등) |
| `repository/` | 유저 JPA 리포지터리 |
| `service/` | 유저·아바타·차단 도메인 서비스 |

## Key Files (domain)

| File | Description |
|------|-------------|
| `User.java` | 핵심 유저 엔티티 (닉네임, 생년월일, 성별, FCM 토큰 등) |
| `Avatar.java` | 아바타 엔티티 |
| `AvatarLine.java` | 아바타 대사 라인 엔티티 |
| `AvatarProfile.java` | 아바타 프로필 조합 엔티티 |
| `AvatarProfileLine.java` | 아바타 프로필-라인 조합 엔티티 |
| `Block.java` | 유저 차단 관계 엔티티 |
| `WithdrawalReason.java` | 탈퇴 사유 엔티티 |

## For AI Agents

### Working In This Directory
- `User` 엔티티는 대부분의 다른 모듈에서 참조합니다. 필드 변경 시 전체 영향 범위를 확인합니다.
- 차단된 유저의 피드·댓글은 조회에서 필터링됩니다 (`feed/repository/` QueryDSL 참고).
- `UserBlockApplication`이 이 모듈의 차단 서비스를 호출합니다.

### Testing Requirements
테스트는 `src/test/java/org/websoso/WSSServer/`에 작성합니다.

## Dependencies

### Internal
- `auth/` — 유저 인증 정보 주입
- `notification/` — FCM 토큰 관리
- `library/` — 서재 기록 연관

<!-- MANUAL: -->
