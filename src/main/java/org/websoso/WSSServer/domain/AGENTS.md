<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-04-03 | Updated: 2026-04-03 -->

# domain

## Purpose
공통 JPA 엔티티 및 도메인 열거형. 특정 기능 모듈에 속하지 않는 공유 도메인 객체(Genre, GenrePreference)와 공통 열거형(CategoryName, AttractivePointName 등)을 관리합니다.

## Key Files

| File | Description |
|------|-------------|
| `Genre.java` | 소설 장르 엔티티 |
| `GenrePreference.java` | 유저 장르 선호도 엔티티 |

## Subdirectories

| Directory | Purpose |
|-----------|---------|
| `common/` | 공통 열거형 및 도메인 유틸리티 클래스 |

## For AI Agents

### Working In This Directory
- 피드·유저·알림·서재 등 기능별 엔티티는 각 모듈 패키지(`feed/domain/`, `user/domain/` 등)에 위치합니다.
- 여러 모듈에서 공유되는 엔티티만 이 패키지에 배치합니다.
- `wss-common`의 `BaseEntity`를 상속하여 `createdAt`, `updatedAt`을 자동 관리합니다.

### Common Patterns
```java
@Entity
@Table(name = "genre")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Genre extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long genreId;
    // ...
}
```

## Dependencies

### Internal
- `wss-common` — BaseEntity

<!-- MANUAL: -->
