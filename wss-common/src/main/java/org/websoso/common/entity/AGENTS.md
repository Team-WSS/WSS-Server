<!-- Parent: ../../../../../../AGENTS.md -->
<!-- Generated: 2026-04-03 | Updated: 2026-04-03 -->

# wss-common/entity

## Purpose
모든 모듈이 공유하는 JPA BaseEntity. 생성 시각(`createdAt`)과 수정 시각(`updatedAt`)을 `@EntityListeners(AuditingEntityListener.class)`로 자동 관리합니다.

## Key Files

| File | Description |
|------|-------------|
| `BaseEntity.java` | 공통 JPA 기반 엔티티. `createdAt`, `updatedAt` 자동 감사 필드 포함 |

## For AI Agents

### Working In This Directory
- 모든 도메인 엔티티는 이 `BaseEntity`를 상속합니다.
- 이 클래스 변경 시 전체 엔티티에 영향을 미칩니다. 신중하게 수정합니다.
- JPA Auditing 활성화는 메인 모듈의 `config/JpaAuditingConfig`에서 `@EnableJpaAuditing`으로 설정합니다.

<!-- MANUAL: -->
