#!/usr/bin/env python3
"""이슈 #620 재현 결과 집계기.

run-race.sh가 두 가지 모드로 호출한다.

  round  한 라운드의 k6 요약 JSON과 DB 최종 상태를 합쳐 CSV 한 줄로 기록한다.
  total  전체 라운드 CSV와 k6 raw CSV를 합쳐 상태 분포와 전체 p95를 낸다.

판정 계약
  이 스크립트는 관측값만 분류한다. 원인은 판정하지 않는다.

  popular_state  popular_feed 행 개수에서 바로 나오는 관측값
      not_registered  0건
      registered      1건
      duplicate_rows  2건 이상 (중복 행이 실제로 남은 직접 관측)
  request_state  요청 단위 이상 여부. popular_state와 분리해서 본다
      clean           4xx / 5xx / 전송 오류 없음, like 개수가 기대치와 같음
      그 외           http_4xx / http_5xx / transport_error / like_count_mismatch 를
                      '+'로 이어 붙인 값. 이 라운드는 popular_state만으로 결론 내리지 않는다

  not_registered가 count == 5 시점을 놓쳐서인지, 리스너의 쓰기가 유실돼서인지,
  5xx가 유니크 제약 충돌 때문인지는 이 스크립트가 알 수 없다.
  원인은 서버 로그와 observe-timeline.sh 결과로 따로 확인한다.

표준 라이브러리만 사용한다.
"""

import argparse
import csv
import glob
import json
import sys
from collections import Counter

POPULAR_NOT_REGISTERED = "not_registered"
POPULAR_REGISTERED = "registered"
POPULAR_DUPLICATE_ROWS = "duplicate_rows"
POPULAR_STATES = (POPULAR_NOT_REGISTERED, POPULAR_REGISTERED, POPULAR_DUPLICATE_ROWS)

REQUEST_CLEAN = "clean"
REQUEST_HTTP_4XX = "http_4xx"
REQUEST_HTTP_5XX = "http_5xx"
REQUEST_TRANSPORT_ERROR = "transport_error"
REQUEST_LIKE_COUNT_MISMATCH = "like_count_mismatch"


def classify_popular_state(popular_count):
    """popular_feed 행 개수만 보고 상태를 정한다. 원인은 판단하지 않는다."""
    if popular_count == 0:
        return POPULAR_NOT_REGISTERED
    if popular_count == 1:
        return POPULAR_REGISTERED
    return POPULAR_DUPLICATE_ROWS


def classify_request_state(status_4xx, status_5xx, transport_error, like_count, expected_likes):
    """요청 단위 이상만 모은다. popular_feed 상태와 섞지 않는다."""
    flags = []
    if status_4xx > 0:
        flags.append(REQUEST_HTTP_4XX)
    if status_5xx > 0:
        flags.append(REQUEST_HTTP_5XX)
    if transport_error > 0:
        flags.append(REQUEST_TRANSPORT_ERROR)
    if like_count != expected_likes:
        flags.append(REQUEST_LIKE_COUNT_MISMATCH)
    return "+".join(flags) if flags else REQUEST_CLEAN


def percentile(sorted_values, ratio):
    """선형 보간 백분위수. k6의 p(95)와 같은 방식이다."""
    if not sorted_values:
        return None
    if len(sorted_values) == 1:
        return sorted_values[0]
    position = ratio * (len(sorted_values) - 1)
    lower = int(position)
    upper = min(lower + 1, len(sorted_values) - 1)
    weight = position - lower
    return sorted_values[lower] * (1 - weight) + sorted_values[upper] * weight


def cmd_round(args):
    with open(args.summary, encoding="utf-8") as f:
        summary = json.load(f)

    status_4xx = int(summary.get("status4xx", 0))
    status_5xx = int(summary.get("status5xx", 0))
    transport_error = int(summary.get("transportError", 0))

    popular_state = classify_popular_state(int(args.popular_count))
    request_state = classify_request_state(
        status_4xx,
        status_5xx,
        transport_error,
        int(args.like_count),
        int(args.expected_likes),
    )

    duration = summary.get("durationMs") or {}
    skew = summary.get("fireSkewMs") or {}

    row = [
        summary.get("round", ""),
        summary.get("staggerMs", 0),
        args.like_count,
        args.popular_count,
        summary.get("status2xx", 0),
        status_4xx,
        status_5xx,
        transport_error,
        summary.get("httpReqFailedRate"),
        duration.get("p95"),
        duration.get("max"),
        skew.get("max"),
        popular_state,
        request_state,
    ]
    writer = csv.writer(sys.stdout, lineterminator="\n")
    writer.writerow(["" if v is None else v for v in row])


def load_raw_durations(pattern):
    """k6 --out csv 결과에서 http_req_duration 값(ms)만 모은다."""
    durations = []
    for path in sorted(glob.glob(pattern)):
        with open(path, encoding="utf-8") as f:
            for record in csv.DictReader(f):
                if record.get("metric_name") == "http_req_duration":
                    try:
                        durations.append(float(record["metric_value"]))
                    except (KeyError, TypeError, ValueError):
                        continue
    return durations


def cmd_total(args):
    popular_states = Counter()
    request_states = Counter()
    rounds = 0
    total_requests = 0
    failed_requests = 0
    dirty_rounds = []

    with open(args.csv, encoding="utf-8") as f:
        for record in csv.DictReader(f):
            rounds += 1
            popular_states[record["popular_state"]] += 1
            request_state = record["request_state"]
            request_states[request_state] += 1
            if request_state != REQUEST_CLEAN:
                dirty_rounds.append("{}({})".format(record["round"], request_state))
            ok = int(record["status_2xx"] or 0)
            bad = (int(record["status_4xx"] or 0)
                   + int(record["status_5xx"] or 0)
                   + int(record["transport_error"] or 0))
            total_requests += ok + bad
            failed_requests += bad

    durations = sorted(load_raw_durations(args.raw_glob))

    result = {
        "rounds": rounds,
        "expectedLikesPerRound": int(args.expected_likes),
        "popularState": {state: popular_states[state] for state in POPULAR_STATES},
        "requestState": dict(sorted(request_states.items())),
        "roundsNeedingManualReview": dirty_rounds,
        "http": {
            "requests": total_requests,
            "failed": failed_requests,
            "failedRate": round(failed_requests / total_requests, 6) if total_requests else None,
        },
        "durationMs": {
            "samples": len(durations),
            "min": round(durations[0], 3) if durations else None,
            "med": round(percentile(durations, 0.50), 3) if durations else None,
            "p95": round(percentile(durations, 0.95), 3) if durations else None,
            "p99": round(percentile(durations, 0.99), 3) if durations else None,
            "max": round(durations[-1], 3) if durations else None,
        },
    }

    if args.json_out:
        with open(args.json_out, "w", encoding="utf-8") as f:
            json.dump(result, f, ensure_ascii=False, indent=2)
            f.write("\n")

    not_registered = result["popularState"][POPULAR_NOT_REGISTERED]
    print("[total] 라운드 {}회".format(rounds))
    print("[total] popular_feed 행 상태  미등록 {} / 1건 {} / 2건 이상 {}".format(
        not_registered,
        result["popularState"][POPULAR_REGISTERED],
        result["popularState"][POPULAR_DUPLICATE_ROWS],
    ))
    print("[total] 미등록 라운드 비율 {:.1%} (관측값이다. 원인은 서버 로그와 타임라인으로 확인한다)".format(
        not_registered / rounds if rounds else 0))
    print("[total] 요청 상태  {}".format(
        " / ".join("{} {}".format(k, v) for k, v in result["requestState"].items()) or "없음"))
    print("[total] HTTP 실패율 {} ({}/{})".format(
        result["http"]["failedRate"], failed_requests, total_requests))
    print("[total] 좋아요 API duration med {} / p95 {} / p99 {} / max {} (ms, n={})".format(
        result["durationMs"]["med"], result["durationMs"]["p95"],
        result["durationMs"]["p99"], result["durationMs"]["max"],
        result["durationMs"]["samples"]))
    if dirty_rounds:
        print("[total] 요청 오류가 있어 수동 확인이 필요한 라운드: {}".format(", ".join(dirty_rounds)))
    if result["popularState"][POPULAR_DUPLICATE_ROWS]:
        print("[total] popular_feed 행이 2건 이상인 라운드가 있다. 서버 로그에서 저장 경로를 확인한다.")
    if args.json_out:
        print("[total] 요약 JSON {}".format(args.json_out))


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    sub = parser.add_subparsers(dest="command", required=True)

    p_round = sub.add_parser("round")
    p_round.add_argument("--summary", required=True)
    p_round.add_argument("--like-count", required=True)
    p_round.add_argument("--popular-count", required=True)
    p_round.add_argument("--expected-likes", required=True)
    p_round.set_defaults(func=cmd_round)

    p_total = sub.add_parser("total")
    p_total.add_argument("--csv", required=True)
    p_total.add_argument("--raw-glob", required=True)
    p_total.add_argument("--expected-likes", required=True)
    p_total.add_argument("--json-out")
    p_total.set_defaults(func=cmd_total)

    args = parser.parse_args()
    args.func(args)


if __name__ == "__main__":
    main()
