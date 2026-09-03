# 동시 좋아요 시 인기 피드 등록 측정 결과 (#620)

같은 피드에 서로 다른 사용자 6명이 거의 동시에 좋아요를 보냈을 때
`popular_feed` 행이 어떤 상태로 남는지, 좋아요 API 응답 시간이 어떤지를 로컬에서 반복 측정했다.
프로덕션 코드와 DB 스키마는 바꾸지 않았다.

재현 스크립트와 실행 방법은 [`scripts/load/popular-feed-like-race/README.md`](../scripts/load/popular-feed-like-race/README.md)에 있다.

## 요약

1. **`popular_feed` 미등록이 100% 재현된다.** 6명 동시 30회 + 발사 간격을 벌린 30회, 총 60회 모두
   `popular_feed` 행이 0건이었다(`popular_state = not_registered`).
   좋아요 자체는 60회 모두 6건씩 정상 저장됐고 요청 오류는 0건(`request_state = clean`)이다.
   이 수치는 관측값이고, 원인은 아래 타임라인으로 따로 확인했다.
2. **동시 요청일 때 임계값 경쟁 (A)이 실제로 성립한다.** 커밋 6건이 0.37ms 안에 끝나고
   AFTER_COMMIT 리스너의 `count(*)`는 그보다 19ms 뒤에 6건이 몰려서 실행됐다.
   전원이 `6`을 읽고 `!= 5`로 빠져나가, `popular_feed` 조회조차 발생하지 않았다.
3. **그런데 동시성을 완전히 없애도 등록되지 않는다.** 400ms 간격으로 서로 다른 사용자 5명이
   한 명씩 좋아요를 보내 최종 `like` 개수를 정확히 5로 만든 대조 실행 5라운드도 전부 `popular_feed` 0건,
   `notification` 0건이었다. 모든 응답은 `204`이고 예외도 로그도 없다.
   5번째 리스너는 `count(*) = 5`를 읽고 임계값을 통과해 `popular_feed` 존재 확인 쿼리까지 갔지만,
   그 뒤 `insert into popular_feed`가 아예 발생하지 않았다.
4. 따라서 **중복 등록 경쟁 (B)는 관측할 수 없었다.** 등록 자체가 어떤 타이밍에서도 성립하지 않기 때문이다.
5. 좋아요 API 응답 시간은 6명 동시 기준 **p95 49.6ms** (med 32.0ms, max 54.7ms, n=180)다.

3번은 이번 이슈가 전제한 동시성 경쟁과는 다른 층위의 결함이다.
이 이슈의 범위(재현 자산 작성)를 벗어나므로 고치지 않고 아래에 후속 이슈 후보로 적어 둔다.

## 측정 환경

| 항목 | 값 |
| --- | --- |
| 대상 | 로컬 `http://localhost:8080`, `./gradlew bootRun` (`local` 프로파일) |
| 호스트 | macOS 26.6, Apple M4 (10 core) |
| 서버 | Spring Boot 3.2.5 / Hibernate 6.4.4 / Java 17 툴체인 |
| DB | MySQL 8.4.11 (Docker `websoso-mysql`, `localhost:3306`) |
| Redis | Redis 8 (Docker `websoso-redis`, `localhost:6379`) |
| 부하 도구 | k6 v2.2.0 (darwin/arm64) |
| 서버 옵션 | `--logging.level.p6spy=OFF --decorator.datasource.p6spy.enable-logging=false` (SQL 로깅으로 응답 시간이 흔들리지 않게 끔) |
| 대상 피드 | `feed_id=331` (`novel_id=1`, 공개, 미숨김) |
| 좋아요 사용자 | 서로 다른 6명 (`user_id` 2~7), 피드 작성자(1)와 차단 관계 없음 |
| 측정일 | 2026-09-03 |

운영·공유 개발 서버는 대상으로 삼지 않았고, 스크립트가 로컬 호스트 외에는 실행을 거부한다.

## 실행 결과

`--stagger-ms`는 VU별 발사 간격이다. `0`이면 6명이 같은 절대 시각에 동시에 쏘고,
키우면 순차 요청에 가까워진다. 모든 라운드는 시작 전에 대상 피드의
`like` / `popular_feed` / `notification` 행을 지워 초기 상태로 되돌린 뒤 실행했다.

표의 `not_registered` / `registered` / `duplicate_rows`는 `popular_feed` 행 개수(0 / 1 / 2 이상)에서
바로 나오는 관측값이다. 원인은 담지 않는다. 요청 오류는 `request_state`로 따로 기록되며
이번 측정에서는 전 라운드가 `clean`이었다.
(측정 당시 집계기는 이 상태들을 `missing` / `registered` / `duplicate_conflict` / `duplicate_rows`로
불렀다. 이후 원인을 단정하지 않도록 이름과 분류를 바꿨고, 위 수치는 그 매핑을 적용한 값이다.
`duplicate_conflict`에 해당하는 라운드는 0건이었으므로 수치 자체는 달라지지 않는다.)

| 실행 | 발사 간격 | 라운드 | `not_registered` | `registered` | `duplicate_rows` | HTTP 실패율 | duration med | **p95** | max |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| A 동시 | 0ms | 30 | **30 (100%)** | 0 | 0 | 0.0 (0/180) | 32.0ms | **49.6ms** | 54.7ms |
| B-1 | 20ms | 10 | **10 (100%)** | 0 | 0 | 0.0 (0/60) | 15.1ms | **28.9ms** | 31.8ms |
| B-2 | 100ms | 10 | **10 (100%)** | 0 | 0 | 0.0 (0/60) | 25.2ms | **37.7ms** | 70.2ms |
| B-3 | 300ms | 10 | **10 (100%)** | 0 | 0 | 0.0 (0/60) | 29.0ms | **36.8ms** | 172.6ms |
| C 대조 (5명) | 400ms | 5 | **5 (100%)** | 0 | 0 | 0.0 (0/25) | 28.9ms | **33.0ms** | 35.4ms |

C는 동시성을 완전히 제거한 대조 실행이다. 사용자 5명이 400ms 간격으로 한 명씩 좋아요를 보내
최종 `like` 개수가 정확히 임계값 5가 되도록 만들었다. 그래도 등록되지 않는다.
`./run-race.sh --vus 5 --stagger-ms 400 --rounds 5` 로 그대로 재현된다.

- 모든 라운드에서 최종 `like` 개수는 참여 사용자 수와 같았고(A·B 6, C 5), `popular_feed` 개수는 0이었다.
  좋아요가 유실된 라운드(`like_count_mismatch`)는 없다.
- 동시 발사 정렬 오차(`fire_skew_max_ms`)는 A 실행 30라운드 중 28라운드가 0ms, 2라운드가 1ms였다.
  즉 "거의 동시"가 아니라 사실상 동시에 도착했다.
- HTTP 5xx와 전송 오류는 전체 385건 중 0건이다. 모든 요청이 `204 No Content`로 끝났다.
- `popular_feed` 행이 2건 이상인 라운드(`duplicate_rows`)는 **0건**이다.

응답 시간은 발사 간격과 상관관계를 보인다. 6명이 완전히 겹치는 A에서 p95가 49.6ms로 가장 높고,
간격을 벌린 B·C에서는 29~38ms 대다. 여기까지가 관측된 상관관계이고, 이 차이가 커넥션 풀 대기인지
Tomcat 스레드 경합인지 DB 쪽 경합인지는 컴포넌트별로 분리 측정하지 않았으므로 단정하지 않는다.
300ms 간격의 max 172.6ms는 한 라운드의 단일 요청에서 나온 값이다. 원인은 확인하지 않았다.

## 근거: 쿼리 타임라인

`observe-timeline.sh`로 MySQL general log를 켜고 한 라운드씩 관찰했다.
general log는 문장마다 기록하므로 이 구간의 응답 시간은 위 측정치와 비교하지 않는다.

### (A) 동시 6건 — 전원이 `6`을 읽는다

```
16:10:14.900338  227027  insert into `like` ... user_id=3
16:10:14.900338  227028  insert into `like` ... user_id=2
16:10:14.900411  227030  insert into `like` ... user_id=5
16:10:14.900624  227025  insert into `like` ... user_id=6
16:10:14.900834  227029  insert into `like` ... user_id=7
16:10:14.900863  227026  insert into `like` ... user_id=4
16:10:14.900857  227030  commit          <- 커밋 6건이
16:10:14.900881  227028  commit
16:10:14.901013  227027  commit
16:10:14.901067  227025  commit
16:10:14.901198  227026  commit
16:10:14.901224  227029  commit          <- 0.37ms 안에 모두 끝난다
16:10:14.920019  227030  select count(l1_0.like_id) from `like` ... feed_id=331
16:10:14.920252  227029  select count(...)      <- 마지막 커밋보다 19ms 뒤,
16:10:14.920422  227025  select count(...)         6건이 0.7ms 안에 몰려서 실행된다
16:10:14.920608  227026  select count(...)
16:10:14.920650  227027  select count(...)
16:10:14.920731  227028  select count(...)
```

판정 구간(count 쿼리 이후)에 `popular_feed` SELECT도 INSERT도 없다.
6개 리스너 전원이 `count = 6`을 읽고 `likeCount != 5`에서 빠져나갔다는 뜻이다.
(이 측정 당시에는 초기화 `DELETE FROM popular_feed`가 타임라인 앞머리에 함께 찍혔다.
지금은 초기화를 general log를 켜기 전에 끝내므로 그 행이 아예 남지 않는다.)

`count(*)`가 커밋보다 19ms 늦은 이유는, 커밋과 리스너 사이에 좋아요 알림 리스너
(`FeedLikeNotificationListener`)가 먼저 실행되면서 차단 확인 / 소설 / 소설 통계 /
알림 타입 / 사용자 / 사용자 기기 조회를 6번 하기 때문이다.
즉 **리스너 판정이 커밋보다 늦어지는 폭이 커질수록 (A) 누락 확률이 올라간다.**
6명의 커밋 간격(0.37ms)보다 커밋→판정 지연(19ms)이 50배 이상 크므로,
동시 요청에서는 누락이 사실상 결정적으로 일어난다.

### (C) 순차 실행 — 임계값을 통과해도 INSERT가 없다

동시성을 제거한 대조 실행이다. 서로 다른 사용자 5명이 400ms 간격으로 한 명씩 좋아요를 보냈다.
같은 조건을 손으로 한 번 더 돌려 초기화 없이 최종 상태를 직접 읽어 보면 이렇다.

```
user=2 http=204   user=3 http=204   user=4 http=204   user=5 http=204   user=6 http=204

likes          5     <- 임계값 조건을 정확히 만족한다
popular_feed   0     <- 그런데 등록되지 않는다
notifications  0     <- 좋아요 알림도 저장되지 않는다
```

왜 간격을 벌려도 누락인지 general log로 확인했다.
`VUS=5 STAGGER_MS=400`으로 한 라운드를 돌리고 마지막(5번째) 요청이 쓴 커넥션만 따라간 것이다.
직전 요청의 커밋은 `02.340644`, 이 요청의 커밋은 `02.741432`로 400ms 떨어져 있다.
요청끼리 겹치는 구간이 전혀 없다.

```
16:20:02.740488  227029  insert into `like` ... user_id=6
16:20:02.741432  227029  commit                     <- 좋아요 트랜잭션 커밋
16:20:02.748622  227029  SET autocommit=1           <- 트랜잭션이 여기서 정리된다
16:20:02.749895  227029  select 1 from block ...            ┐
16:20:02.750972  227029  select ... from novel ...          │ FeedLikeNotificationListener
16:20:02.751480  227029  select ... from novel_statistics   │ (AFTER_COMMIT)
16:20:02.752373  227029  select ... from notification_type  │
16:20:02.753151  227029  select ... from user ...           │
16:20:02.754046  227029  select ... from user_device ...    ┘  <- insert into notification 없음
16:20:02.755002  227029  select count(l1_0.like_id) from `like` ... feed_id=331
                          <- PopularFeedCheckEventListener. count = 5 이므로 임계값 통과
16:20:02.756076  227029  select pf1_0.popular_feed_id from popular_feed pf1_0 where pf1_0.feed_id=331 limit 1
                          <- existByFeed. 결과 없음(false) 이므로 create()로 진행
                          <- general log는 여기서 끝난다. insert into popular_feed 가 없다
```

`popular_feed` 조회가 로그 전체의 마지막 문장이다.
`likeCount != 5`도 `existByFeed`도 통과해 `popularFeedService.create(feed)`까지 갔는데
INSERT가 DB로 나가지 않았다. 즉 **간격을 벌려도 누락인 이유는 임계값 경쟁이 아니다.**
(A)는 동시 요청에서만 성립하고, 순차 요청의 누락은 이 별개 원인 때문이다.

공통점은 커밋 직후의 `SET autocommit=1`이다.
AFTER_COMMIT 이후 문장이 전부 autocommit 상태에서 실행되고 그 뒤로 `SET autocommit=0`이 다시 나오지 않는다.
좋아요 트랜잭션이나 그 앞의 사용자 조회 트랜잭션이 시작될 때는 `SET autocommit=0`이 찍히는 것과 대비된다.
리스너 안의 `@Transactional` 메서드들이 **새 트랜잭션을 열지 않은 채** 실행됐다는 뜻이다.
읽기는 autocommit으로 그대로 동작하지만, `save()`로 만든 엔티티는 flush될 자리가 없어
그대로 사라지는 것으로 보인다. 그래서 `notification`도 `popular_feed`도 INSERT가 나가지 않는다.
여기까지가 general log로 확인한 사실이고, 왜 새 트랜잭션이 열리지 않는지(전파 속성인지,
이미 완료된 트랜잭션 자원이 스레드에 남아 있어서인지)는 후속 이슈에서 코드로 확인해야 한다.

응답은 `204`, 서버 로그에 ERROR도 WARN도 없다. 조용히 유실된다.
측정 기간 전체에 걸쳐 서버 로그의 ERROR는 0건, WARN은 기동 시 2건(`open-in-view`, 임시 보안 비밀번호)뿐이었다.

## 해석

| 가설 | 결과 | 근거 |
| --- | --- | --- |
| (A) `count == 5` 체크 누락 | **성립 확인** | 동시 6건에서 커밋 6건(0.37ms) 뒤 19ms 지나 count 6건이 모두 `6`을 읽음. `popular_feed` 조회 미발생 |
| (B) `exist` 확인과 `save` 사이 경쟁 | **관측 불가** | 등록 자체가 성립하지 않아 두 리스너가 동시에 저장을 시도하는 상황이 만들어지지 않음. 전 라운드에서 5xx 0건, `duplicate_rows` 0건 |
| (C) AFTER_COMMIT 리스너 쓰기 유실 | **신규 발견** | 동시성 없는 대조 실행(400ms 간격 5명)에서 `like`=5인데 `popular_feed`=0, `notification`=0. general log에 INSERT 자체가 없음 |

`AFTER_COMMIT 리스너는 요청 스레드에서 동기 실행된다`는 가설은 맞다. 근거는 두 가지이고 층위가 다르다.

- **코드 계약** — `PopularFeedCheckEventListener`와 `FeedLikeNotificationListener` 어디에도 `@Async`가 없고,
  `applicationEventMulticaster`에 `TaskExecutor`를 주입하는 설정도 없다. 기본 이벤트 전달은 호출 스레드에서
  수행되므로 두 리스너는 요청 스레드에서 동기 실행된다. 동기 실행의 근거는 이쪽이다.
- **general log 관측** — 커밋 뒤에 리스너 SQL이 같은 `thread_id`로 이어서 찍힌다.
  `thread_id`는 MySQL 커넥션 식별자이므로 이것으로 말할 수 있는 것은 리스너 쿼리가
  좋아요 트랜잭션과 **같은 DB 커넥션**에서 이어졌다는 사실까지다. JVM 요청 스레드와 같다는 뜻은 아니다.

(B)를 관측하려면 (C)가 먼저 해결되어야 한다.
(C)가 해결된 뒤에도 (B)는 (A)보다 훨씬 좁은 창을 노려야 한다.
두 리스너가 **모두** `5`를 읽어야 하는데, 위 타임라인에서 보듯 커밋들은 서로 0.4ms 안에 붙고
판정은 그보다 19ms 뒤에 몰려서 실행되기 때문이다.
로컬 스키마의 `popular_feed.feed_id`에는 유니크 키(`UK_ggr3ta3ux4vhitypguhgc6y13`)가 있다.
(B)가 실제로 일어난다면 중복 행이 아니라 무결성 위반 예외(응답 5xx)로 드러날 것으로 보이지만,
이번 측정에서는 5xx가 한 건도 없어 확인하지 못했다.
집계기는 5xx를 `request_state = http_5xx`로만 기록하고 유니크 충돌이라고 단정하지 않는다.
그 원인은 서버 로그에서 해당 예외가 실제로 보일 때만 쓴다.

## 후속 이슈 후보

이번 이슈 범위 밖이라 고치지 않았다.

1. **AFTER_COMMIT 리스너 안의 쓰기가 저장되지 않는다.**
   `PopularFeedService.create`, `NotificationService.create*` 모두 해당한다.
   인기 피드 등록과 모든 푸시 알림 기록이 동작하지 않는 상태로 보인다.
   영향 범위가 넓으므로 먼저 실제 dev 환경에서 같은 증상인지 확인이 필요하다.
   (`select count(*) from notification where created_date > ...` 로 빠르게 확인할 수 있다.)
2. **`likeCount != 5` 등호 비교.** (C)를 고쳐도 동시 요청에서는 전원이 `6`을 읽어 누락된다.
   임계값 비교 방식 자체를 다시 볼 필요가 있다.
3. **`existByFeed` 확인과 `create` 저장 사이의 비원자성.** (1),(2)를 고친 뒤 남는 문제다.
4. **좋아요 응답 시간에 알림 리스너가 동기로 포함된다.** 커밋 이후 조회 6건이 요청 스레드에서
   그대로 이어져 응답 시간에 들어간다. 응답 시간에서 이 구간이 차지하는 비중은 분리 측정하지 않았다.

## 재현 방법

```bash
cd scripts/load/popular-feed-like-race

# 1) 로컬 픽스처 (최초 1회)
DB_DOCKER_CONTAINER=websoso-mysql DB_PASSWORD=1234 ./seed-fixture.sh

# 2) 반복 측정
export FEED_ID=<위 출력값> USER_IDS=<위 출력값>
export JWT_SECRET='<config-repo/application-local.yml의 jwt.secret>'
export DB_DOCKER_CONTAINER=websoso-mysql DB_PASSWORD=1234
./run-race.sh --rounds 30 --stagger-ms 0            # A: 6명 동시
./run-race.sh --rounds 5 --vus 5 --stagger-ms 400   # C: 동시성 없는 대조

# 3) 쿼리 타임라인 (원인 확인용)
./observe-timeline.sh                      # 동시 6건: 전원이 count=6을 읽는 것을 본다
VUS=5 STAGGER_MS=400 ./observe-timeline.sh # 순차 5건: count=5를 읽고도 INSERT가 없는 것을 본다
```

## 한계

- 로컬 단일 인스턴스 측정이다. 여러 인스턴스가 뜨는 환경에서는 리스너가 서로 다른 JVM에서 돌아
  (A)와 (B)의 창이 달라질 수 있다.
- 로컬 스키마는 `ddl-auto: update`로 만들어졌다. `popular_feed.feed_id` 유니크 키가
  dev/운영 스키마에도 있는지는 확인하지 않았다. 없다면 (B)는 5xx가 아니라 중복 행으로 나타난다.
- MySQL general log를 켠 상태의 타임라인은 순서 관찰용이며 응답 시간 근거로 쓰지 않았다.
