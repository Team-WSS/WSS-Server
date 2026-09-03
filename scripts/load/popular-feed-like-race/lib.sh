#!/usr/bin/env bash
# 이슈 #620 재현 스크립트 공용 함수.
# 직접 실행하지 않고 seed-fixture.sh / run-race.sh 에서 source 한다.

set -euo pipefail

# --- 환경 변수 기본값 -------------------------------------------------------
BASE_URL="${BASE_URL:-http://localhost:8080}"

DB_HOST="${DB_HOST:-127.0.0.1}"
DB_PORT="${DB_PORT:-3306}"
DB_USER="${DB_USER:-root}"
DB_NAME="${DB_NAME:-websoso}"
# DB_PASSWORD 는 기본값을 두지 않는다. 없으면 빈 문자열로 접속을 시도한다.
DB_PASSWORD="${DB_PASSWORD:-}"
# 로컬 MySQL이 컨테이너 안에만 있으면 컨테이너 이름을 지정한다. 예: websoso-mysql
DB_DOCKER_CONTAINER="${DB_DOCKER_CONTAINER:-}"

# --- 안전 가드 --------------------------------------------------------------
# 운영/공유 개발 서버를 대상으로는 절대 실행하지 않는다. 우회 옵션을 두지 않는다.
LOCAL_HOSTS_REGEX='^(localhost|127\.0\.0\.1|\[::1\]|::1|0\.0\.0\.0|host\.docker\.internal)$'

extract_host() {
    # http://host:port/path -> host
    printf '%s' "$1" | sed -E 's#^[a-zA-Z]+://##; s#/.*$##; s#:[0-9]+$##; s#^.*@##'
}

assert_local_target() {
    local url_host db_host
    url_host="$(extract_host "${BASE_URL}")"
    db_host="$(extract_host "${DB_HOST}")"

    if ! printf '%s' "${url_host}" | grep -Eq "${LOCAL_HOSTS_REGEX}"; then
        echo "거부: BASE_URL 호스트가 로컬이 아니다 (${url_host}). 이 스크립트는 로컬/비운영 환경 전용이다." >&2
        exit 2
    fi
    if [[ -z "${DB_DOCKER_CONTAINER}" ]] \
        && ! printf '%s' "${db_host}" | grep -Eq "${LOCAL_HOSTS_REGEX}"; then
        echo "거부: DB_HOST가 로컬이 아니다 (${db_host}). 공유 DB에 대한 초기화는 수행하지 않는다." >&2
        exit 2
    fi
}

# --- MySQL 실행 -------------------------------------------------------------
mysql_exec() {
    # stdin으로 받은 SQL을 실행하고 결과를 탭 구분 텍스트(헤더 없음)로 출력한다.
    if [[ -n "${DB_DOCKER_CONTAINER}" ]]; then
        docker exec -i "${DB_DOCKER_CONTAINER}" \
            mysql -u"${DB_USER}" ${DB_PASSWORD:+-p"${DB_PASSWORD}"} \
            -D "${DB_NAME}" --default-character-set=utf8mb4 -N -B \
            2> >(grep -v 'Using a password on the command line' >&2)
    else
        mysql -h "${DB_HOST}" -P "${DB_PORT}" -u "${DB_USER}" \
            ${DB_PASSWORD:+-p"${DB_PASSWORD}"} \
            -D "${DB_NAME}" --default-character-set=utf8mb4 -N -B \
            2> >(grep -v 'Using a password on the command line' >&2)
    fi
}

mysql_scalar() {
    # $1: SQL. 첫 컬럼 첫 행만 반환한다.
    printf '%s\n' "$1" | mysql_exec | head -n 1
}

assert_db_reachable() {
    if ! printf 'SELECT 1;\n' | mysql_exec >/dev/null; then
        echo "거부: MySQL에 접속할 수 없다. DB_HOST/DB_PORT/DB_USER/DB_PASSWORD/DB_NAME 또는 DB_DOCKER_CONTAINER를 확인한다." >&2
        exit 2
    fi
}

require_env() {
    local name="$1"
    if [[ -z "${!name:-}" ]]; then
        echo "거부: 환경 변수 ${name}가 필요하다." >&2
        exit 2
    fi
}

# --- 입력 검증 --------------------------------------------------------------
# 아래 값들은 SQL 문자열이나 셸 명령에 그대로 들어간다.
# 조회와 DELETE보다 먼저 형식을 확인해서 의도하지 않은 문장이 만들어지지 않게 한다.

# 1 이상의 정수. 앞자리 0이나 부호, 공백을 허용하지 않는다.
require_positive_int() {
    local name="$1" value="$2"
    if ! [[ "${value}" =~ ^[1-9][0-9]*$ ]]; then
        echo "거부: ${name}는 1 이상의 정수여야 한다 (현재 값: '${value}')." >&2
        exit 2
    fi
}

# 0 이상의 정수.
require_non_negative_int() {
    local name="$1" value="$2"
    if ! [[ "${value}" =~ ^(0|[1-9][0-9]*)$ ]]; then
        echo "거부: ${name}는 0 이상의 정수여야 한다 (현재 값: '${value}')." >&2
        exit 2
    fi
}

# SQL 문자열 리터럴에 들어가는 식별자. 영숫자와 밑줄만 허용한다.
require_sql_safe_tag() {
    local name="$1" value="$2" max_length="$3"
    if ! [[ "${value}" =~ ^[A-Za-z0-9_]+$ ]]; then
        echo "거부: ${name}는 영문자·숫자·밑줄만 쓸 수 있다 (현재 값: '${value}')." >&2
        exit 2
    fi
    if [[ ${#value} -gt ${max_length} ]]; then
        echo "거부: ${name}는 ${max_length}자 이하여야 한다 (현재 길이: ${#value})." >&2
        exit 2
    fi
}

# 피드 PK. 모든 조회와 DELETE 앞에서 호출한다.
require_feed_id() {
    require_positive_int "FEED_ID" "${1:-}"
}

# --- 피드 상태 조회/초기화 --------------------------------------------------
feed_like_count() {
    require_feed_id "${1:-}"
    mysql_scalar "SELECT COUNT(*) FROM \`like\` WHERE feed_id = ${1};"
}

popular_feed_count() {
    require_feed_id "${1:-}"
    mysql_scalar "SELECT COUNT(*) FROM popular_feed WHERE feed_id = ${1};"
}

feed_exists() {
    require_feed_id "${1:-}"
    mysql_scalar "SELECT COUNT(*) FROM feed WHERE feed_id = ${1};"
}

# 대상 피드 한 건에만 한정된 초기화다. 다른 피드나 사용자 데이터는 건드리지 않는다.
reset_feed_state() {
    require_feed_id "${1:-}"
    local feed_id="$1"
    printf '%s\n' "
        DELETE rn FROM read_notification rn
            JOIN notification n ON n.notification_id = rn.notification_id
            WHERE n.feed_id = ${feed_id};
        DELETE FROM notification WHERE feed_id = ${feed_id};
        DELETE FROM popular_feed WHERE feed_id = ${feed_id};
        DELETE FROM \`like\` WHERE feed_id = ${feed_id};
    " | mysql_exec
}
