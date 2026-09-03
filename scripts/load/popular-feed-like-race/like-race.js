/*
 * 이슈 #620 재현 시나리오
 *
 * 하나의 피드에 서로 다른 사용자 N명(기본 6명)이 거의 동시에 좋아요를 보낸다.
 * 모든 VU는 setup()이 정한 공통 시작 시각까지 대기했다가 동시에 POST를 발사한다.
 *
 * 프로덕션 코드는 건드리지 않는다. 이 스크립트는 관측만 한다.
 *
 * 필수 환경 변수
 *   BASE_URL   대상 서버 (예: http://localhost:8080). 로컬/비운영 환경만 사용한다.
 *   FEED_ID    좋아요 대상 피드 PK
 *
 * 토큰 지정 방식 (둘 중 하나)
 *   TOKENS       콤마로 구분한 Access Token 목록. VU 수만큼 필요하다.
 *   JWT_SECRET   + USER_IDS. 로컬에서 Access Token을 직접 서명해 만든다.
 *                서명 방식은 JwtProvider/JwtKeyProvider 계약과 동일하다.
 *                HMAC 키는 시크릿 원문이 아니라 Base64(시크릿 UTF-8)의 ASCII 바이트다.
 *
 * 선택 환경 변수
 *   VUS              동시 사용자 수 (기본 6)
 *   START_DELAY_MS   setup() 이후 동시 발사까지의 리드타임 (기본 1500)
 *   SPIN_MS          발사 직전 busy-spin 구간 (기본 20). 이 구간만 sleep 없이 돈다.
 *   STAGGER_MS       VU별 발사 간격 (기본 0 = 완전 동시). n번째 VU는 (n-1)*STAGGER_MS만큼 늦게 쏜다.
 *                    0에서 키워 가며 어느 간격부터 판정이 정상으로 돌아오는지 관찰할 수 있다.
 *   ROUND            반복 회차 라벨. 결과 JSON에 그대로 기록된다.
 *   SUMMARY_PATH     라운드 요약 JSON 출력 경로 (기본 stdout만)
 *   TOKEN_TTL_SEC    JWT_SECRET 모드에서 발급할 토큰 수명 (기본 600)
 */

import { sleep } from 'k6';
import http from 'k6/http';
import crypto from 'k6/crypto';
import encoding from 'k6/encoding';
import exec from 'k6/execution';
import { Counter, Trend } from 'k6/metrics';

const BASE_URL = requireEnv('BASE_URL').replace(/\/+$/, '');
const FEED_ID = requireEnv('FEED_ID');
const VUS = intEnv('VUS', 6);
const START_DELAY_MS = intEnv('START_DELAY_MS', 1500);
const SPIN_MS = intEnv('SPIN_MS', 20);
const STAGGER_MS = intEnv('STAGGER_MS', 0);
const ROUND = __ENV.ROUND || '1';
const SUMMARY_PATH = __ENV.SUMMARY_PATH || '';
const TOKEN_TTL_SEC = intEnv('TOKEN_TTL_SEC', 600);

const TOKENS = resolveTokens(VUS);

const likeDuration = new Trend('like_req_duration', true);
const likeStatus2xx = new Counter('like_status_2xx');
const likeStatus4xx = new Counter('like_status_4xx');
const likeStatus5xx = new Counter('like_status_5xx');
const likeTransportError = new Counter('like_transport_error');
const fireSkew = new Trend('like_fire_skew_ms', true);

export const options = {
    scenarios: {
        simultaneous_like: {
            executor: 'per-vu-iterations',
            vus: VUS,
            iterations: 1,
            maxDuration: '60s',
        },
    },
    // 이 시나리오는 임계값으로 실행을 중단시키지 않는다. 판정은 DB 상태로 한다.
    thresholds: {},
    summaryTrendStats: ['avg', 'min', 'med', 'p(90)', 'p(95)', 'p(99)', 'max'],
    discardResponseBodies: false,
};

export function setup() {
    return { startAtMs: Date.now() + START_DELAY_MS };
}

export default function (data) {
    const vu = exec.vu.idInTest; // 1..VUS
    const token = TOKENS[(vu - 1) % TOKENS.length];

    const targetAtMs = data.startAtMs + (vu - 1) * STAGGER_MS;
    waitUntil(targetAtMs);

    const firedAt = Date.now();
    fireSkew.add(firedAt - targetAtMs);

    const res = http.post(
        `${BASE_URL}/feeds/${FEED_ID}/likes`,
        null,
        {
            headers: { Authorization: `Bearer ${token}` },
            tags: { name: 'POST /feeds/{feedId}/likes', round: ROUND },
            timeout: '30s',
        },
    );

    likeDuration.add(res.timings.duration);

    if (res.error_code !== 0 && res.status === 0) {
        likeTransportError.add(1);
    } else if (res.status >= 500) {
        likeStatus5xx.add(1);
    } else if (res.status >= 400) {
        likeStatus4xx.add(1);
    } else {
        likeStatus2xx.add(1);
    }

    console.log(JSON.stringify({
        round: ROUND,
        vu,
        staggerMs: STAGGER_MS,
        firedAtMs: firedAt,
        skewMs: firedAt - targetAtMs,
        status: res.status,
        errorCode: res.error_code,
        durationMs: Math.round(res.timings.duration * 1000) / 1000,
        body: res.status >= 400 ? String(res.body || '').slice(0, 500) : '',
    }));
}

export function handleSummary(summary) {
    const duration = summary.metrics.like_req_duration || { values: {} };
    const skew = summary.metrics.like_fire_skew_ms || { values: {} };
    const httpFailed = summary.metrics.http_req_failed || { values: {} };

    const round = {
        round: ROUND,
        vus: VUS,
        staggerMs: STAGGER_MS,
        feedId: FEED_ID,
        requests: counter(summary, 'like_status_2xx') + counter(summary, 'like_status_4xx')
            + counter(summary, 'like_status_5xx') + counter(summary, 'like_transport_error'),
        status2xx: counter(summary, 'like_status_2xx'),
        status4xx: counter(summary, 'like_status_4xx'),
        status5xx: counter(summary, 'like_status_5xx'),
        transportError: counter(summary, 'like_transport_error'),
        httpReqFailedRate: numberOrNull(httpFailed.values && httpFailed.values.rate),
        durationMs: {
            avg: numberOrNull(duration.values['avg']),
            min: numberOrNull(duration.values['min']),
            med: numberOrNull(duration.values['med']),
            p90: numberOrNull(duration.values['p(90)']),
            p95: numberOrNull(duration.values['p(95)']),
            p99: numberOrNull(duration.values['p(99)']),
            max: numberOrNull(duration.values['max']),
        },
        fireSkewMs: {
            min: numberOrNull(skew.values['min']),
            med: numberOrNull(skew.values['med']),
            max: numberOrNull(skew.values['max']),
        },
    };

    const out = { stdout: `round ${ROUND}: ${JSON.stringify(round)}\n` };
    if (SUMMARY_PATH) {
        out[SUMMARY_PATH] = JSON.stringify(round, null, 2);
    }
    return out;
}

// ---------------------------------------------------------------- helpers

function counter(summary, name) {
    const metric = summary.metrics[name];
    return metric && metric.values ? metric.values.count || 0 : 0;
}

function numberOrNull(value) {
    return typeof value === 'number' && isFinite(value) ? Math.round(value * 1000) / 1000 : null;
}

function requireEnv(name) {
    const value = __ENV[name];
    if (!value) {
        throw new Error(`환경 변수 ${name}가 필요하다.`);
    }
    return value;
}

function intEnv(name, fallback) {
    const raw = __ENV[name];
    if (raw === undefined || raw === '') return fallback;
    const parsed = parseInt(raw, 10);
    if (isNaN(parsed)) {
        throw new Error(`환경 변수 ${name}는 정수여야 한다: ${raw}`);
    }
    return parsed;
}

/**
 * 목표 시각까지 대기한다.
 * 대기 대부분은 sleep으로 CPU를 양보하고, 마지막 SPIN_MS 구간만 busy-spin으로 정렬한다.
 * 전 구간을 busy-spin으로 돌면 VU들이 CPU를 잡아먹어 측정 자체가 흔들린다.
 */
function waitUntil(targetAtMs) {
    for (;;) {
        const remainingMs = targetAtMs - Date.now() - SPIN_MS;
        if (remainingMs <= 0) break;
        sleep(Math.min(remainingMs, 100) / 1000);
    }
    while (Date.now() < targetAtMs) {
        // 최종 정렬 구간
    }
}

/**
 * TOKENS가 있으면 그대로 쓰고, 없으면 JWT_SECRET + USER_IDS로 Access Token을 서명한다.
 * 토큰은 절대 저장소에 커밋하지 않는다. 환경 변수로만 주입한다.
 */
function resolveTokens(required) {
    const raw = __ENV.TOKENS;
    if (raw) {
        const tokens = raw.split(',').map((t) => t.trim()).filter(Boolean);
        if (tokens.length < required) {
            throw new Error(`TOKENS에 ${required}개가 필요하다. 현재 ${tokens.length}개.`);
        }
        return tokens;
    }

    const secret = __ENV.JWT_SECRET;
    const userIds = (__ENV.USER_IDS || '').split(',').map((v) => v.trim()).filter(Boolean);
    if (!secret || userIds.length === 0) {
        throw new Error('TOKENS 또는 (JWT_SECRET + USER_IDS)를 지정해야 한다.');
    }
    if (userIds.length < required) {
        throw new Error(`USER_IDS에 ${required}개가 필요하다. 현재 ${userIds.length}개.`);
    }
    if (new Set(userIds).size !== userIds.length) {
        throw new Error('USER_IDS에 중복된 사용자 ID가 있다. 서로 다른 사용자여야 한다.');
    }
    return userIds.map((id) => signAccessToken(secret, id));
}

/**
 * JwtProvider와 동일한 Access Token을 만든다.
 *   - alg HS256, typ JWT
 *   - sub "access", iat/exp(초), userId 클레임
 *   - HMAC 키는 JwtKeyProvider와 같이 Base64(secret UTF-8) 문자열의 바이트다.
 */
function signAccessToken(secret, userId) {
    const nowSec = Math.floor(Date.now() / 1000);
    const header = { typ: 'JWT', alg: 'HS256' };
    const payload = {
        sub: 'access',
        iat: nowSec,
        exp: nowSec + TOKEN_TTL_SEC,
        userId: parseInt(userId, 10),
    };

    const signingInput = `${b64url(JSON.stringify(header))}.${b64url(JSON.stringify(payload))}`;
    const key = encoding.b64encode(secret, 'std');
    const signature = crypto.hmac('sha256', key, signingInput, 'base64rawurl');
    return `${signingInput}.${signature}`;
}

function b64url(text) {
    return encoding.b64encode(text, 'rawurl');
}
