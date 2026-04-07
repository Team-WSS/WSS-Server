<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-04-03 | Updated: 2026-04-03 -->

# user/domain

## Purpose
유저 관련 JPA 엔티티. 핵심 유저 정보, 아바타 시스템, 차단 관계, 탈퇴 사유를 엔티티로 모델링합니다.

## Key Files

| File | Description |
|------|-------------|
| `User.java` | 핵심 유저 엔티티 (닉네임, 성별, 생년월일, FCM 토큰, 프로필 이미지, 장르 필터링 여부 등) |
| `Avatar.java` | 아바타 엔티티 (아바타 이미지 정보) |
| `AvatarLine.java` | 아바타 대사 라인 엔티티 |
| `AvatarProfile.java` | 아바타 프로필 조합 (아바타 + 배경) 엔티티 |
| `AvatarProfileLine.java` | 아바타 프로필에 연결된 대사 라인 엔티티 |
| `Block.java` | 유저 차단 관계 엔티티 (차단자·피차단자 양방향) |
| `WithdrawalReason.java` | 탈퇴 사유 열거형 엔티티 |

## For AI Agents

### Working In This Directory
- `User` 엔티티는 전체 시스템의 핵심입니다. 필드 추가·변경 시 DB 마이그레이션과 관련 서비스 전체를 검토합니다.
- 차단(`Block`) 관계는 피드·댓글 조회 필터링에 광범위하게 영향을 줍니다.

<!-- MANUAL: -->
