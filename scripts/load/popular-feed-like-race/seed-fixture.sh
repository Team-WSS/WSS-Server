#!/usr/bin/env bash
# 이슈 #620 재현용 로컬 픽스처를 만든다.
#
# 만드는 것
#   - 아바타/아바타 프로필 1건 (사용자 생성 제약 충족용)
#   - 알림 타입 3건 (좋아요 / 댓글 / 지금뜨는글). 없으면 좋아요 알림 저장이 실패한다.
#   - 소설 1건 (인기 피드 판정은 novelId가 있는 피드만 대상으로 한다)
#   - 작성자 1명 + 좋아요를 누를 사용자 N명 (기본 6명)
#   - 대상 피드 1건
#
# 이미 만들어 둔 픽스처가 있으면 다시 만들지 않고 기존 ID를 그대로 출력한다.
# 로컬 전용이다. 원격/공유 DB에서는 실행을 거부한다.
#
# 사용법
#   DB_DOCKER_CONTAINER=websoso-mysql DB_PASSWORD=... ./seed-fixture.sh
#   LIKER_COUNT=6 ./seed-fixture.sh

set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=lib.sh
source "${SCRIPT_DIR}/lib.sh"

LIKER_COUNT="${LIKER_COUNT:-6}"
FIXTURE_TAG="${FIXTURE_TAG:-race620}"

# FIXTURE_TAG는 아래 INSERT의 SQL 문자열 리터럴에 그대로 들어간다.
# 영숫자와 밑줄만 허용하고, nickname 컬럼이 varchar(10)이라 태그는 7자 이하로 제한한다.
require_sql_safe_tag "FIXTURE_TAG" "${FIXTURE_TAG}" 7
require_positive_int "LIKER_COUNT" "${LIKER_COUNT}"

assert_local_target
assert_db_reachable

echo "[seed] 대상 DB: ${DB_DOCKER_CONTAINER:-${DB_HOST}:${DB_PORT}}/${DB_NAME}"

# --- 공통 마스터 데이터 -----------------------------------------------------
printf '%s\n' "
    INSERT INTO avatar (avatar_id, avatar_image, avatar_name)
        SELECT 1, 'seed', 'seed'
        FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM avatar WHERE avatar_id = 1);

    INSERT INTO avatar_profile (avatar_profile_id, avatar_character_image, avatar_profile_image, avatar_profile_name, avatar_id)
        SELECT 1, 'seed', 'seed', 'seed', 1
        FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM avatar_profile WHERE avatar_profile_id = 1);

    INSERT INTO notification_type (notification_type_image, notification_type_name)
        SELECT 'seed', '좋아요'
        FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM notification_type WHERE notification_type_name = '좋아요');
    INSERT INTO notification_type (notification_type_image, notification_type_name)
        SELECT 'seed', '댓글'
        FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM notification_type WHERE notification_type_name = '댓글');
    INSERT INTO notification_type (notification_type_image, notification_type_name)
        SELECT 'seed', '지금뜨는글'
        FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM notification_type WHERE notification_type_name = '지금뜨는글');

    INSERT INTO novel (author, is_completed, novel_description, novel_image, title)
        SELECT '${FIXTURE_TAG}', 0, '${FIXTURE_TAG} 재현용', 'seed', '${FIXTURE_TAG}-novel'
        FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM novel WHERE title = '${FIXTURE_TAG}-novel');
" | mysql_exec

NOVEL_ID="$(mysql_scalar "SELECT novel_id FROM novel WHERE title = '${FIXTURE_TAG}-novel' LIMIT 1;")"

# --- 사용자 -----------------------------------------------------------------
create_user() {
    local nickname="$1"
    printf '%s\n' "
        INSERT INTO user (created_date, modified_date, avatar_id, avatar_profile_id, birth, email,
                          gender, intro, is_profile_public, is_push_enabled, marketing_agreed,
                          nickname, privacy_agreed, role, service_agreed, social_id)
            SELECT NOW(6), NOW(6), 1, 1, 2000, NULL,
                   'M', '${FIXTURE_TAG}', 1, 1, 0,
                   '${nickname}', 1, 'USER', 1, '${FIXTURE_TAG}-${nickname}'
            FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM user WHERE nickname = '${nickname}');
    " | mysql_exec
    mysql_scalar "SELECT user_id FROM user WHERE nickname = '${nickname}' LIMIT 1;"
}

WRITER_NICKNAME="${FIXTURE_TAG}w"
WRITER_ID="$(create_user "${WRITER_NICKNAME}")"

LIKER_IDS=()
for ((i = 1; i <= LIKER_COUNT; i++)); do
    LIKER_IDS+=("$(create_user "${FIXTURE_TAG}l${i}")")
done
USER_IDS="$(IFS=,; echo "${LIKER_IDS[*]}")"

# --- 대상 피드 ---------------------------------------------------------------
FEED_CONTENT="${FIXTURE_TAG}-target-feed"
printf '%s\n' "
    INSERT INTO feed (created_date, feed_content, is_hidden, is_public, is_spoiler, modified_date, novel_id, user_id)
        SELECT NOW(6), '${FEED_CONTENT}', 0, 1, b'0', NOW(6), ${NOVEL_ID}, ${WRITER_ID}
        FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM feed WHERE feed_content = '${FEED_CONTENT}');
" | mysql_exec

FEED_ID="$(mysql_scalar "SELECT feed_id FROM feed WHERE feed_content = '${FEED_CONTENT}' LIMIT 1;")"

cat <<EOF

[seed] 완료
  NOVEL_ID  = ${NOVEL_ID}
  WRITER_ID = ${WRITER_ID}
  FEED_ID   = ${FEED_ID}
  USER_IDS  = ${USER_IDS}

다음처럼 재현을 실행한다.

  export FEED_ID=${FEED_ID}
  export USER_IDS=${USER_IDS}
  export JWT_SECRET='<config-repo/application-local.yml의 jwt.secret>'
  ./run-race.sh --rounds 20
EOF
