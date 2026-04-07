<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-04-03 | Updated: 2026-04-03 -->

# application

## Purpose
유스케이스 오케스트레이션 레이어. 여러 서비스/리포지터리를 조합하여 하나의 비즈니스 흐름(유스케이스)을 완성하는 Application 클래스들을 모아둡니다. 컨트롤러에서 직접 호출되며 트랜잭션 경계를 관리합니다.

## Key Files

| File | Description |
|------|-------------|
| `AccountApplication.java` | 회원가입·탈퇴·약관 동의 등 계정 관리 유스케이스 |
| `AppVersionApplication.java` | 앱 최소 버전 조회 유스케이스 |
| `AuthApplication.java` | 토큰 재발급·FCM 토큰 저장·로그아웃 유스케이스 |
| `CommentFindApplication.java` | 댓글 조회 유스케이스 |
| `CommentManagementApplication.java` | 댓글 작성·수정·삭제 유스케이스 |
| `FeedFindApplication.java` | 피드 조회(전체·상세·인기) 유스케이스 |
| `FeedManagementApplication.java` | 피드 작성·수정·삭제·좋아요 유스케이스 |
| `LibraryEvaluationApplication.java` | 작품 평가(등록·수정·삭제) 유스케이스 |
| `LibraryInterestApplication.java` | 관심글·관심 작품 등록/삭제 유스케이스 |
| `NotificationApplication.java` | 알림 조회·읽음 처리 유스케이스 |
| `NotificationSendApplication.java` | 알림 발송 오케스트레이션 |
| `PopularNovelScheduler.java` | 인기 소설 스케줄러 (주기적 집계) |
| `ReportApplication.java` | 피드·댓글 신고 유스케이스 |
| `SearchNovelApplication.java` | 소설 검색 유스케이스 |
| `UserBlockApplication.java` | 유저 차단·차단 해제 유스케이스 |

## For AI Agents

### Working In This Directory
- Application 클래스는 **서비스 호출 조합**에 집중합니다. 도메인 로직은 Service/Domain에, 데이터 접근은 Repository에 위임합니다.
- 각 Application 클래스는 하나의 기능 영역에 집중합니다 (예: `FeedFindApplication`은 조회만, `FeedManagementApplication`은 CUD만).
- `@Transactional`은 Application 레이어에서 선언합니다.

### Common Patterns
```java
@RequiredArgsConstructor
@Service
public class FooApplication {
    private final FooService fooService;
    private final BarService barService;

    @Transactional
    public void doSomething(Long userId, FooRequest request) {
        // 여러 서비스를 조합하여 유스케이스 완성
    }
}
```

## Dependencies

### Internal
- `feed/service/`, `user/service/`, `library/service/` 등 각 도메인 서비스
- `notification/service/` — 알림 발송

<!-- MANUAL: -->
