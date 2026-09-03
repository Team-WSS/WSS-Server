#!/usr/bin/env bash
# 이슈 #620 재현 실행기.
#
# 한 라운드는 아래 순서로 진행한다.
#   1) 대상 피드에 묶인 like / popular_feed / notification 행만 삭제해 초기 상태로 되돌린다
#   2) k6로 서로 다른 사용자 N명이 같은 시각에 좋아요 POST를 발사한다
#   3) DB 최종 상태(like 개수, popular_feed 개수)와 HTTP 응답을 관측값으로 기록한다
#
# 기록하는 것 (관측값만. 원인은 판정하지 않는다)
#   popular_state  not_registered(0건) / registered(1건) / duplicate_rows(2건 이상)
#   request_state  clean, 또는 http_4xx / http_5xx / transport_error / like_count_mismatch 조합
#                  request_state가 clean이 아니면 popular_state만으로 결론 내리지 않는다
#
#   미등록이 count == 5 시점을 놓쳐서인지 리스너 쓰기가 유실돼서인지,
#   5xx가 유니크 제약 충돌인지는 서버 로그와 observe-timeline.sh로 따로 확인한다.
#
# 로컬 전용이다. 원격/공유 서버와 DB에는 실행을 거부한다.
#
# 사용법
#   FEED_ID=1 USER_IDS=1,2,3,4,5,6 JWT_SECRET='...' \
#   DB_DOCKER_CONTAINER=websoso-mysql DB_PASSWORD=1234 \
#     ./run-race.sh --rounds 20
#
#   토큰을 직접 발급해 쓰려면 USER_IDS/JWT_SECRET 대신 TOKENS=a,b,c,... 를 넘긴다.

set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=lib.sh
source "${SCRIPT_DIR}/lib.sh"

ROUNDS="${ROUNDS:-10}"
VUS="${VUS:-6}"
START_DELAY_MS="${START_DELAY_MS:-1500}"
SPIN_MS="${SPIN_MS:-20}"
STAGGER_MS="${STAGGER_MS:-0}"
SETTLE_MS="${SETTLE_MS:-1000}"
OUT_DIR="${OUT_DIR:-}"
K6_BIN="${K6_BIN:-k6}"

usage() {
    sed -n '2,28p' "$0"
    exit "${1:-0}"
}

while [[ $# -gt 0 ]]; do
    case "$1" in
        --rounds) ROUNDS="$2"; shift 2 ;;
        --vus) VUS="$2"; shift 2 ;;
        --start-delay-ms) START_DELAY_MS="$2"; shift 2 ;;
        --stagger-ms) STAGGER_MS="$2"; shift 2 ;;
        --settle-ms) SETTLE_MS="$2"; shift 2 ;;
        --out) OUT_DIR="$2"; shift 2 ;;
        -h|--help) usage 0 ;;
        *) echo "알 수 없는 옵션: $1" >&2; usage 2 ;;
    esac
done

require_env FEED_ID
if [[ -z "${TOKENS:-}" ]]; then
    require_env USER_IDS
    require_env JWT_SECRET
fi

# DB 조회와 DELETE가 실행되기 전에 숫자 입력을 먼저 검증한다.
require_feed_id "${FEED_ID}"
require_positive_int "ROUNDS" "${ROUNDS}"
require_positive_int "VUS" "${VUS}"
require_non_negative_int "START_DELAY_MS" "${START_DELAY_MS}"
require_non_negative_int "SPIN_MS" "${SPIN_MS}"
require_non_negative_int "STAGGER_MS" "${STAGGER_MS}"
require_non_negative_int "SETTLE_MS" "${SETTLE_MS}"

if ! command -v "${K6_BIN}" >/dev/null 2>&1; then
    echo "거부: k6를 찾을 수 없다 (${K6_BIN}). 설치 후 다시 실행한다." >&2
    exit 2
fi

assert_local_target
assert_db_reachable

if [[ "$(feed_exists "${FEED_ID}")" != "1" ]]; then
    echo "거부: feed_id=${FEED_ID} 가 존재하지 않는다. seed-fixture.sh로 픽스처를 먼저 만든다." >&2
    exit 2
fi

if ! curl -sS -o /dev/null -m 5 "${BASE_URL}/actuator/health" 2>/dev/null; then
    echo "경고: ${BASE_URL}/actuator/health 응답을 확인하지 못했다. 서버 기동 상태를 확인한다." >&2
fi

if [[ -z "${OUT_DIR}" ]]; then
    OUT_DIR="${SCRIPT_DIR}/results/$(date +%Y%m%d-%H%M%S)"
fi
mkdir -p "${OUT_DIR}"

CSV="${OUT_DIR}/rounds.csv"
echo "round,stagger_ms,like_count,popular_count,status_2xx,status_4xx,status_5xx,transport_error,http_failed_rate,p95_ms,max_ms,fire_skew_max_ms,popular_state,request_state" > "${CSV}"

echo "[run] 대상 ${BASE_URL} / feed_id=${FEED_ID} / vus=${VUS} / rounds=${ROUNDS} / stagger=${STAGGER_MS}ms"
echo "[run] 결과 디렉터리 ${OUT_DIR}"

for ((round = 1; round <= ROUNDS; round++)); do
    reset_feed_state "${FEED_ID}"

    before_like="$(feed_like_count "${FEED_ID}")"
    before_popular="$(popular_feed_count "${FEED_ID}")"
    if [[ "${before_like}" != "0" || "${before_popular}" != "0" ]]; then
        echo "거부: 초기화 후에도 like=${before_like}, popular_feed=${before_popular} 이다. 중단한다." >&2
        exit 3
    fi

    summary_json="${OUT_DIR}/round-${round}.json"
    raw_csv="${OUT_DIR}/round-${round}-raw.csv"
    k6_log="${OUT_DIR}/round-${round}-k6.log"

    set +e
    BASE_URL="${BASE_URL}" \
    FEED_ID="${FEED_ID}" \
    VUS="${VUS}" \
    START_DELAY_MS="${START_DELAY_MS}" \
    SPIN_MS="${SPIN_MS}" \
    STAGGER_MS="${STAGGER_MS}" \
    ROUND="${round}" \
    SUMMARY_PATH="${summary_json}" \
    TOKENS="${TOKENS:-}" \
    USER_IDS="${USER_IDS:-}" \
    JWT_SECRET="${JWT_SECRET:-}" \
        "${K6_BIN}" run --quiet --no-usage-report \
            --out "csv=${raw_csv}" \
            "${SCRIPT_DIR}/like-race.js" > "${k6_log}" 2>&1
    k6_exit=$?
    set -e

    if [[ ${k6_exit} -ne 0 || ! -f "${summary_json}" ]]; then
        echo "실패: 라운드 ${round} k6 실행 오류(exit=${k6_exit}). ${k6_log} 참고." >&2
        tail -n 20 "${k6_log}" >&2
        exit 4
    fi

    # DB 최종 상태 관측을 안정화하기 위한 유예다.
    # 현재 구현의 AFTER_COMMIT 리스너에는 @Async가 없어 요청 스레드에서 동기 실행되므로
    # 응답이 돌아온 시점에는 리스너가 이미 끝나 있다. 즉 필수 대기는 아니다.
    python3 -c "import time,sys; time.sleep(int(sys.argv[1])/1000)" "${SETTLE_MS}"

    like_count="$(feed_like_count "${FEED_ID}")"
    popular_count="$(popular_feed_count "${FEED_ID}")"

    python3 "${SCRIPT_DIR}/summarize.py" round \
        --summary "${summary_json}" \
        --like-count "${like_count}" \
        --popular-count "${popular_count}" \
        --expected-likes "${VUS}" >> "${CSV}"

    tail -n 1 "${CSV}"
done

echo
python3 "${SCRIPT_DIR}/summarize.py" total \
    --csv "${CSV}" \
    --raw-glob "${OUT_DIR}/round-*-raw.csv" \
    --expected-likes "${VUS}" \
    --json-out "${OUT_DIR}/summary.json"
