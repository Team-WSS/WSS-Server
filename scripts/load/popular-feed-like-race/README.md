# 인기 피드 등록 동시성 재현 (#620)

같은 피드에 서로 다른 사용자 6명이 거의 동시에 좋아요를 보냈을 때
`popular_feed` 행이 어떤 상태로 남는지, 그리고 좋아요 API 응답 시간이 어떤지를
반복 측정하기 위한 자산이다. 프로덕션 코드는 바꾸지 않고 관측만 한다.
관측값만 기록하고 원인은 판정하지 않는다.

측정 결과는 [`docs/popular-feed-like-race-report.md`](../../../docs/popular-feed-like-race-report.md)에 있다.

## 무엇을 보는가

좋아요 요청은 `FeedLikeApplication.create`의 트랜잭션이 커밋된 뒤
`PopularFeedCheckEventListener`(AFTER_COMMIT)가 요청 스레드에서 그대로 이어서 실행한다.
그 안의 `PopularFeedApplication.checkAndRegister`는 이렇게 판정한다.

```
likeCount = count(like where feed_id = ?)
if (likeCount != 5) return;          // (A) 정확히 5일 때만 통과한다
if (popularFeedService.existByFeed) return;   // (B) 확인과
popularFeedService.create(feed);              // (B) 저장 사이가 원자적이지 않다
```

확인하려는 가설은 둘이다. **가설이지 이 스크립트의 판정 결과가 아니다.**

- **(A) 임계값 경쟁** — 6개 트랜잭션이 모두 커밋된 뒤에야 리스너들이 카운트를 읽으면
  모두 `6`을 보고 `!= 5`로 빠져나갈 수 있다.
- **(B) exist/save 경쟁** — 두 리스너가 모두 `5`를 읽으면 둘 다 임계값을 통과하고,
  `exist` 확인과 `save` 사이에서 경쟁한다.
  유니크 키가 있는 스키마라면 두 번째 저장이 무결성 위반으로 터지고,
  없는 스키마라면 행이 2건 남는다.

`run-race.sh`는 이 가설들을 자동으로 판정하지 않는다.
`popular_feed` 행 개수와 요청 오류 여부를 **관측값으로만** 기록한다.
어느 가설이 성립했는지, 5xx가 무엇 때문이었는지는 서버 로그와 `observe-timeline.sh`의
쿼리 순서로 따로 확인해야 한다.

> 로컬 실측에서는 (A), (B)와 별개로, 리스너가 임계값과 존재 확인을 통과한 뒤에도
> `popular_feed` INSERT가 발생하지 않는 현상을 관측했다. 자세한 근거는 결과 문서를 본다.

## 사전 조건

- 로컬(또는 개인 전용 비운영) 서버가 떠 있어야 한다. **운영·공유 개발 서버 대상 실행은 스크립트가 거부한다.**
- 로컬 MySQL 접근. `mysql` 클라이언트가 있거나, 컨테이너 이름을 `DB_DOCKER_CONTAINER`로 넘길 수 있어야 한다.
- [k6](https://k6.io) (`brew install k6`)
- `python3` (집계용. 표준 라이브러리만 쓴다)

측정 시에는 SQL 로깅이 응답 시간을 크게 흔들기 때문에 꺼 두고 서버를 띄우는 편이 낫다.

```bash
./gradlew bootRun --args='--logging.level.p6spy=OFF --decorator.datasource.p6spy.enable-logging=false'
```

## 환경 변수

| 변수 | 필수 | 기본값 | 설명 |
| --- | --- | --- | --- |
| `FEED_ID` | O | – | 좋아요 대상 피드 PK. `novel_id`가 있는 피드여야 인기 피드 판정 대상이 된다 |
| `TOKENS` | △ | – | 콤마로 구분한 Access Token 목록. VU 수만큼 필요하다 |
| `USER_IDS` | △ | – | `TOKENS` 대신 쓸 사용자 PK 목록. `JWT_SECRET`과 함께 쓴다 |
| `JWT_SECRET` | △ | – | `USER_IDS` 모드에서 Access Token을 서명할 시크릿 |
| `BASE_URL` | | `http://localhost:8080` | 대상 서버. 로컬 호스트만 허용한다 |
| `DB_HOST` / `DB_PORT` / `DB_USER` / `DB_PASSWORD` / `DB_NAME` | | `127.0.0.1` / `3306` / `root` / (빈 값) / `websoso` | 판정용 DB 접속 정보 |
| `DB_DOCKER_CONTAINER` | | – | 지정하면 `docker exec <이름> mysql`로 접속한다 |
| `K6_BIN` | | `k6` | k6 실행 파일 경로 |

`TOKENS` 또는 (`USER_IDS` + `JWT_SECRET`) 중 하나는 반드시 있어야 한다.
**어떤 값도 저장소에 커밋하지 않는다. 전부 환경 변수로만 주입한다.**

`USER_IDS` 모드는 `JwtProvider`/`JwtKeyProvider`와 같은 방식으로 토큰을 만든다.
HMAC 키가 시크릿 원문이 아니라 `Base64(시크릿 UTF-8)` 문자열의 바이트라는 점까지 맞춰 놓았다.
로그인 API를 6번 태우지 않고도 서로 다른 사용자 6명을 만들 수 있어 반복 실행이 쉽다.

## 실행

### 1. 픽스처 만들기 (로컬에서 처음 한 번)

소설 1건, 작성자 1명, 좋아요를 누를 사용자 6명, 대상 피드 1건과
알림 타입 마스터 데이터를 만든다. 이미 있으면 다시 만들지 않는다.

```bash
cd scripts/load/popular-feed-like-race
DB_DOCKER_CONTAINER=websoso-mysql DB_PASSWORD=1234 ./seed-fixture.sh
```

출력에 나온 `FEED_ID`와 `USER_IDS`를 그대로 다음 단계에 쓴다.

### 2. 반복 실행

```bash
export FEED_ID=331
export USER_IDS=2,3,4,5,6,7
export JWT_SECRET='<config-repo/application-local.yml의 jwt.secret>'
export DB_DOCKER_CONTAINER=websoso-mysql DB_PASSWORD=1234

./run-race.sh --rounds 30
```

한 라운드는 이렇게 돈다.

1. 대상 피드에 묶인 `like` / `popular_feed` / `notification` 행만 지워 초기 상태로 되돌린다
2. k6가 VU 6개를 공통 시작 시각까지 대기시켰다가 동시에 `POST /feeds/{feedId}/likes`를 쏜다
3. `--settle-ms`만큼 유예를 둔 뒤 DB 최종 상태를 읽어 관측값으로 기록한다

### 옵션

| 옵션 | 기본값 | 설명 |
| --- | --- | --- |
| `--rounds` | 10 | 반복 횟수 |
| `--vus` | 6 | 동시 사용자 수. 임계값(5)보다 하나 많아야 경쟁이 생긴다 |
| `--stagger-ms` | 0 | VU별 발사 간격. `0`이면 완전 동시, 키우면 순차에 가까워진다 |
| `--start-delay-ms` | 1500 | 공통 시작 시각까지의 리드타임 |
| `--settle-ms` | 1000 | DB 최종 상태를 읽기 전 유예. 현재 구현에서는 필수 대기가 아니다 |
| `--out` | `results/<timestamp>` | 결과 디렉터리 |

`--settle-ms`는 DB 최종 상태 관측을 안정화하기 위한 유예다.
현재 구현의 `PopularFeedCheckEventListener`에는 `@Async`가 없고 이벤트가 호출 스레드에서
전달되므로, 응답이 돌아온 시점에는 리스너가 이미 끝나 있다. 그래서 이 대기는 필수가 아니며
`0`으로 두어도 된다. 리스너를 비동기로 바꾸는 변경이 들어오면 다시 필요해질 수 있어 옵션으로 남겨 둔다.

동시 시작은 VU마다 공통 절대 시각을 정해 두고 맞춘다.
대기 대부분은 `sleep()`으로 CPU를 양보하고 마지막 `SPIN_MS`(기본 20ms)만 busy-spin으로 정렬한다.
전 구간을 busy-spin으로 돌리면 VU들이 CPU를 잡아먹어 측정 자체가 흔들리기 때문이다.
`fire_skew_max_ms` 컬럼이 실제 발사 시각이 목표에서 얼마나 벗어났는지 보여준다.

### 3. 쿼리 타임라인 관찰 (선택)

판정이 왜 그렇게 났는지 서버가 실제로 보낸 쿼리 순서로 확인하고 싶을 때 쓴다.
MySQL general log를 잠깐 켰다가 한 라운드를 돌리고, 대상 피드와 관련된 문장만 시간순으로 뽑는다.

```bash
FEED_ID=331 USER_IDS=2,3,4,5,6,7 JWT_SECRET='...' \
DB_DOCKER_CONTAINER=websoso-mysql DB_PASSWORD=1234 ./observe-timeline.sh
```

```bash
# 동시 6건: 리스너 6개가 모두 count = 6을 읽는다
./observe-timeline.sh

# 순차 5건: 마지막 리스너가 count = 5를 읽고 존재 확인까지 가는 것을 본다
VUS=5 STAGGER_MS=400 ./observe-timeline.sh
```

`STAGGER_MS`를 바꿔 가며 돌리면 동시일 때와 순차일 때 리스너가 읽는 값이 어떻게 달라지는지 볼 수 있다.
general log는 문장마다 기록하므로 이 모드의 응답 시간은 `run-race.sh` 측정치와 비교하지 않는다.
끝나면 원래 general log 설정으로 되돌린다.
기존 `mysql.general_log` 행은 지우지 않고, marker 이후 구간만 잘라서 본다.
라운드 초기화는 general log를 켜기 전에 끝내므로 초기화 `DELETE`는 타임라인에 섞이지 않는다.

## 결과 읽기

`<out>/rounds.csv`에 라운드마다 한 줄씩 쌓인다.

```
round,stagger_ms,like_count,popular_count,status_2xx,status_4xx,status_5xx,
transport_error,http_failed_rate,p95_ms,max_ms,fire_skew_max_ms,popular_state,request_state
```

상태는 두 축으로 나뉜다. **둘 다 관측값이고, 원인은 담지 않는다.**

`popular_state` — `popular_feed` 행 개수에서 바로 나온다.

| popular_state | 조건 | 뜻 |
| --- | --- | --- |
| `not_registered` | `popular_count == 0` | 인기 피드 행이 없다. 그 이상은 말하지 않는다 |
| `registered` | `popular_count == 1` | 인기 피드 행이 1건 있다 |
| `duplicate_rows` | `popular_count >= 2` | 중복 행이 실제로 남았다. 직접 관측이다 |

`request_state` — 요청 단위 이상 여부. `popular_state`와 분리해서 본다.

| request_state | 조건 |
| --- | --- |
| `clean` | 4xx / 5xx / 전송 오류 없음, `like_count == --vus` |
| `http_4xx` / `http_5xx` / `transport_error` / `like_count_mismatch` | 해당 오류가 있음. 여러 개면 `+`로 이어 붙는다 |

`request_state`가 `clean`이 아닌 라운드는 `popular_state`만으로 결론을 내리지 않는다.
집계 출력이 그 라운드 번호를 따로 뽑아 준다.

원인은 이 표에 없다. 예를 들어

- `not_registered`가 `count == 5` 시점을 놓쳐서인지, 리스너의 쓰기가 유실돼서인지
- `http_5xx`가 유니크 제약 충돌 때문인지 다른 예외 때문인지

는 서버 로그의 예외와 `observe-timeline.sh`의 쿼리 순서로 확인한다.
유니크 충돌은 서버 로그에서 해당 예외가 실제로 보일 때만 원인으로 쓴다.

`<out>/summary.json`과 마지막 `[total]` 출력에 전체 집계가 있다.

- `popular_state` 분포와 미등록 라운드 비율 (관측값)
- `request_state` 분포와 수동 확인이 필요한 라운드 목록
- HTTP 실패율 (`4xx + 5xx + 전송 오류` / 전체 요청)
- 전체 라운드의 `http_req_duration` 원시 값을 합쳐 계산한 med / p95 / p99 / max

라운드별 원시 데이터는 `round-N-raw.csv`(k6 `--out csv`),
k6 콘솔 로그는 `round-N-k6.log`, k6 요약은 `round-N.json`에 남는다.

`results/`는 git에 올라가지 않는다.

## 안전 규칙

- `BASE_URL`과 `DB_HOST`가 로컬 호스트가 아니면 실행을 거부한다. 우회 옵션은 두지 않았다.
- 라운드 초기화 `DELETE`는 전부 `feed_id = <대상>` 으로 범위를 좁힌다.
  다른 피드, 다른 사용자, 다른 테이블 데이터는 건드리지 않는다.
- 초기화 직후 `like`/`popular_feed`가 0이 아니면 즉시 중단한다.
- **SQL에 들어가는 값은 조회와 `DELETE`보다 먼저 형식을 검증한다.**
  - `FEED_ID`는 1 이상의 정수여야 한다. `feed_like_count` / `popular_feed_count` /
    `feed_exists` / `reset_feed_state` 각각이 실행 직전에 다시 확인한다.
  - `ROUNDS` / `VUS` / `LIKER_COUNT`는 1 이상의 정수,
    `START_DELAY_MS` / `SPIN_MS` / `STAGGER_MS` / `SETTLE_MS`는 0 이상의 정수여야 한다.
  - `FIXTURE_TAG`는 SQL 문자열 리터럴에 들어가므로 영숫자와 밑줄만, 7자 이하만 허용한다.
- **`observe-timeline.sh`는 `mysql.general_log`를 지우지 않는다.**
  라운드 시작 직전에 남긴 marker 행 시각 이후의 행만 필터링하므로
  이미 쌓여 있던 로컬 general log가 보존된다.
- 토큰·시크릿·DB 비밀번호는 전부 환경 변수로만 받는다.

## 자동 검증

`PopularFeedLikeRaceScriptTest`가 `./gradlew test`에서 아래를 정적으로 확인한다.
서버나 DB, k6 없이 돈다.

- 재현 자산 파일이 모두 있는지
- k6가 호출하는 경로가 `FeedController#likeFeed`의 실제 매핑과 같은지
- 기본 VU 수가 `PopularFeedApplication`의 임계값 + 1인지
- 반복 횟수·동시 시작 조절 옵션이 살아 있는지
- 초기화 `DELETE`가 `feed_id`로 한정되는지, 로컬 가드가 있는지
- 숫자 입력과 `FIXTURE_TAG` 검증이 걸려 있는지, 피드 조회·삭제 함수가 `FEED_ID`를 다시 확인하는지
- `observe-timeline.sh`가 `mysql.general_log`를 `TRUNCATE`하지 않고 marker로 구간을 자르는지
- 집계기가 원인을 단정하는 상태값을 쓰지 않고 `popular_state`와 `request_state`를 분리하는지
- 토큰이나 시크릿 리터럴이 섞여 들어가지 않았는지

> 이 테스트는 스크립트 파일을 Gradle `test` 태스크의 입력으로 선언하지 않았다.
> 자바 코드를 건드리지 않고 스크립트만 고치면 `test`가 UP-TO-DATE로 건너뛴다.
> 스크립트를 고친 뒤에는 `./gradlew test --tests "*PopularFeedLikeRaceScriptTest*" --rerun-tasks`
> 로 확인한다.
