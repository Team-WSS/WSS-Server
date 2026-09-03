#!/usr/bin/env bash
# 한 라운드를 MySQL general log와 함께 실행해 서버가 실제로 어떤 순서로 쿼리를 보냈는지 본다.
#
# 무엇을 알 수 있나
#   - 좋아요 INSERT/COMMIT이 얼마나 촘촘히 붙어 있는지
#   - AFTER_COMMIT 리스너의 count(*)가 커밋들보다 앞서는지 뒤서는지
#   - 판정 구간에 popular_feed SELECT/INSERT가 나오는지, 어디까지 진행되고 멈추는지
#
# 주의
#   general log는 문장마다 기록하기 때문에 응답 시간을 늘린다.
#   이 모드에서 나온 latency는 run-race.sh의 측정치와 비교하지 않는다.
#   로컬 전용이며, 끝나면 general log 설정을 원래대로 되돌린다.
#
#   mysql.general_log 테이블은 지우지 않는다. 라운드 시작 직전에 남긴 marker 행을 찾아
#   그 시각 이후의 행만 필터링하므로, 이미 쌓여 있던 로컬 general log는 그대로 보존된다.
#
# 사용법
#   FEED_ID=331 USER_IDS=2,3,4,5,6,7 JWT_SECRET='...' \
#   DB_DOCKER_CONTAINER=websoso-mysql DB_PASSWORD=1234 ./observe-timeline.sh

set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=lib.sh
source "${SCRIPT_DIR}/lib.sh"

VUS="${VUS:-6}"
STAGGER_MS="${STAGGER_MS:-0}"
OUT_DIR="${OUT_DIR:-${SCRIPT_DIR}/results/timeline-$(date +%Y%m%d-%H%M%S)}"
K6_BIN="${K6_BIN:-k6}"

require_env FEED_ID
if [[ -z "${TOKENS:-}" ]]; then
    require_env USER_IDS
    require_env JWT_SECRET
fi

# DB 조회와 DELETE가 실행되기 전에 숫자 입력을 먼저 검증한다.
require_feed_id "${FEED_ID}"
require_positive_int "VUS" "${VUS}"
require_non_negative_int "STAGGER_MS" "${STAGGER_MS}"

assert_local_target
assert_db_reachable
mkdir -p "${OUT_DIR}"

# 이번 실행 구간을 찾기 위한 marker. 영숫자와 밑줄만 쓴다.
MARKER="WSS620_MARKER_$(date +%s)_$$"
require_sql_safe_tag "MARKER" "${MARKER}" 64

PREV_LOG_OUTPUT="$(mysql_scalar "SELECT @@global.log_output;")"
PREV_GENERAL_LOG="$(mysql_scalar "SELECT @@global.general_log;")"

restore_general_log() {
    printf '%s\n' "
        SET GLOBAL general_log = ${PREV_GENERAL_LOG};
        SET GLOBAL log_output = '${PREV_LOG_OUTPUT}';
    " | mysql_exec || true
}
trap restore_general_log EXIT

# 초기화는 general log를 켜기 전에 끝낸다. 초기화 DELETE가 타임라인에 섞이지 않게 한다.
reset_feed_state "${FEED_ID}"

echo "[timeline] general log를 켠다 (원래 값: log_output=${PREV_LOG_OUTPUT}, general_log=${PREV_GENERAL_LOG})"
echo "[timeline] 기존 general log 행은 지우지 않는다. marker=${MARKER}"
printf '%s\n' "
    SET GLOBAL log_output = 'TABLE';
    SET GLOBAL general_log = 'ON';
    SELECT '${MARKER}' AS marker;
" | mysql_exec > /dev/null

BASE_URL="${BASE_URL}" \
FEED_ID="${FEED_ID}" \
VUS="${VUS}" \
STAGGER_MS="${STAGGER_MS}" \
ROUND="timeline" \
SUMMARY_PATH="${OUT_DIR}/round.json" \
TOKENS="${TOKENS:-}" \
USER_IDS="${USER_IDS:-}" \
JWT_SECRET="${JWT_SECRET:-}" \
    "${K6_BIN}" run --quiet --no-usage-report "${SCRIPT_DIR}/like-race.js" \
    > "${OUT_DIR}/k6.log" 2>&1

python3 -c "import time; time.sleep(1.5)"

printf '%s\n' "SET GLOBAL general_log = 'OFF';" | mysql_exec

LIKE_COUNT="$(feed_like_count "${FEED_ID}")"
POPULAR_COUNT="$(popular_feed_count "${FEED_ID}")"

# marker 행 이후, 대상 피드와 관련된 문장만 시간순으로 뽑는다.
# marker를 심은 문장 자체와 general log를 읽는 문장은 결과에서 제외한다.
printf '%s\n' "
    SET @marker_at := (
        SELECT MIN(event_time) FROM mysql.general_log
        WHERE command_type = 'Query'
          AND CONVERT(argument USING utf8mb4) LIKE '%${MARKER}%'
    );
    SELECT DATE_FORMAT(event_time, '%H:%i:%s.%f') AS at,
           thread_id,
           REPLACE(REPLACE(LEFT(CONVERT(argument USING utf8mb4), 160), '\n', ' '), '\t', ' ') AS stmt
    FROM mysql.general_log
    WHERE command_type = 'Query'
      AND @marker_at IS NOT NULL
      AND event_time >= @marker_at
      AND CONVERT(argument USING utf8mb4) NOT LIKE '%${MARKER}%'
      AND CONVERT(argument USING utf8mb4) NOT LIKE '%general_log%'
      AND (
            CONVERT(argument USING utf8mb4) LIKE '%popular_feed%'
         OR CONVERT(argument USING utf8mb4) LIKE '%\`like\`%'
         OR CONVERT(argument USING utf8mb4) LIKE 'commit'
         OR CONVERT(argument USING utf8mb4) LIKE 'SET autocommit%'
      )
    ORDER BY event_time, thread_id;
" | mysql_exec > "${OUT_DIR}/timeline.tsv"

cat <<EOF

[timeline] like_count=${LIKE_COUNT}, popular_feed_count=${POPULAR_COUNT}
[timeline] 타임라인 ${OUT_DIR}/timeline.tsv ($(wc -l < "${OUT_DIR}/timeline.tsv" | tr -d ' ') 줄)

읽는 법
  - 초기화 DELETE는 general log를 켜기 전에 실행되므로 이 타임라인에 없다.
  - insert into \`like\` ... 뒤에 오는 commit이 각 요청의 커밋 시점이다.
  - 그 다음 select count(*) from \`like\` ... 가 AFTER_COMMIT 리스너의 판정 쿼리다.
  - count 쿼리들이 커밋들 뒤에 몰려 있으면 모두 같은 값을 읽었다는 뜻이다.
  - 판정 구간(count 쿼리 이후)에 popular_feed SELECT나 INSERT가 있는지 확인한다.
    어디까지 나오고 어디서 멈추는지가 관측값이고, 원인은 여기서 단정하지 않는다.
EOF
