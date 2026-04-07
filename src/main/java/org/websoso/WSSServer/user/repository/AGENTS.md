<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-04-03 | Updated: 2026-04-03 -->

# user/repository

## Purpose
유저 도메인 데이터 접근 레이어. User, Avatar, Block, WithdrawalReason 엔티티의 JPA 리포지터리를 제공합니다.

## Key Files

| File | Description |
|------|-------------|
| `UserRepository.java` | 유저 JPA 리포지터리 (닉네임 중복 확인, ID 조회 등) |
| `AvatarRepository.java` | 아바타 JPA 리포지터리 |
| `AvatarProfileRepository.java` | 아바타 프로필 JPA 리포지터리 |
| `BlockRepository.java` | 차단 관계 JPA 리포지터리 |
| `WithdrawalReasonRepository.java` | 탈퇴 사유 JPA 리포지터리 |

## For AI Agents

### Working In This Directory
- `UserRepository`는 전체 시스템에서 가장 많이 참조되는 리포지터리입니다. 메서드 추가 시 네이밍 컨벤션을 일관되게 유지합니다.
- 복잡한 쿼리가 필요하면 `UserCustomRepository` + `UserCustomRepositoryImpl` 패턴으로 확장합니다.

<!-- MANUAL: -->
