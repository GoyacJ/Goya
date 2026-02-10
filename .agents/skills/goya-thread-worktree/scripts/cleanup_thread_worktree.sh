#!/usr/bin/env bash
set -euo pipefail

DEFAULT_WORKTREE_ROOT="/Users/goya/.codex/worktrees/goya"

usage() {
  cat <<'EOF'
Usage:
  cleanup_thread_worktree.sh [--thread-id <id>] [--topic <topic>] [--worktree-root <path>] [--dry-run]

Examples:
  cleanup_thread_worktree.sh --thread-id 019c46a4-82e0-72f0-910d-e9e612ba221e --topic security-core
  cleanup_thread_worktree.sh --thread-id 019c46a4-82e0-72f0-910d-e9e612ba221e --dry-run
EOF
}

fail() {
  echo "[ERROR] $*" >&2
  exit 1
}

slugify() {
  local value
  value="$(printf '%s' "$1" | tr '[:upper:]' '[:lower:]')"
  value="$(printf '%s' "$value" | sed -E 's/[^a-z0-9]+/-/g; s/^-+//; s/-+$//; s/-+/-/g')"
  printf '%s' "$value"
}

run_cmd() {
  if [[ "${DRY_RUN}" == "true" ]]; then
    printf '[dry-run]'
    printf ' %q' "$@"
    printf '\n'
  else
    "$@"
  fi
}

THREAD_ID=""
TOPIC=""
WORKTREE_ROOT="${DEFAULT_WORKTREE_ROOT}"
DRY_RUN="false"

while [[ $# -gt 0 ]]; do
  case "$1" in
    --thread-id)
      [[ $# -ge 2 ]] || fail "--thread-id requires a value"
      THREAD_ID="$2"
      shift 2
      ;;
    --topic)
      [[ $# -ge 2 ]] || fail "--topic requires a value"
      TOPIC="$2"
      shift 2
      ;;
    --worktree-root)
      [[ $# -ge 2 ]] || fail "--worktree-root requires a value"
      WORKTREE_ROOT="$2"
      shift 2
      ;;
    --dry-run)
      DRY_RUN="true"
      shift
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      fail "Unknown argument: $1"
      ;;
  esac
done

if [[ -z "${THREAD_ID}" && -z "${TOPIC}" ]]; then
  fail "Provide --thread-id or --topic (or both)"
fi

REPO_ROOT="$(git rev-parse --show-toplevel 2>/dev/null || true)"
[[ -n "${REPO_ROOT}" ]] || fail "Not inside a git repository"

CURRENT_WORKTREE="$(git rev-parse --show-toplevel)"
THREAD_SHORT=""
TOPIC_SLUG=""

if [[ -n "${THREAD_ID}" ]]; then
  THREAD_SLUG="$(slugify "${THREAD_ID}")"
  [[ -n "${THREAD_SLUG}" ]] || fail "Invalid thread-id: ${THREAD_ID}"
  THREAD_SHORT="${THREAD_SLUG:0:12}"
fi

if [[ -n "${TOPIC}" ]]; then
  TOPIC_SLUG="$(slugify "${TOPIC}")"
  [[ -n "${TOPIC_SLUG}" ]] || fail "Invalid topic: ${TOPIC}"
fi

ALL_WORKTREES=()
while IFS= read -r wt; do
  ALL_WORKTREES+=("${wt}")
done < <(git worktree list --porcelain | awk '/^worktree / {print substr($0,10)}')

MATCHED=()
for wt in "${ALL_WORKTREES[@]}"; do
  case "${wt}" in
    "${WORKTREE_ROOT%/}"/*)
      base_name="$(basename "${wt}")"
      if [[ -n "${THREAD_SHORT}" && "${base_name}" != "${THREAD_SHORT}-"* ]]; then
        continue
      fi
      if [[ -n "${TOPIC_SLUG}" && "${base_name}" != *"-${TOPIC_SLUG}" ]]; then
        continue
      fi
      MATCHED+=("${wt}")
      ;;
  esac
done

if [[ ${#MATCHED[@]} -eq 0 ]]; then
  fail "No managed worktree matched under ${WORKTREE_ROOT}"
fi

if [[ ${#MATCHED[@]} -gt 1 ]]; then
  printf '[ERROR] Multiple worktrees matched:\n' >&2
  printf '  - %s\n' "${MATCHED[@]}" >&2
  fail "Please provide both --thread-id and --topic to narrow down"
fi

TARGET_WORKTREE="${MATCHED[0]}"

if [[ "${TARGET_WORKTREE}" == "${CURRENT_WORKTREE}" ]]; then
  fail "Refuse to remove current active worktree: ${TARGET_WORKTREE}"
fi

run_cmd git worktree remove "${TARGET_WORKTREE}"
run_cmd git worktree prune

cat <<EOF
[OK] Thread worktree removed
repo_root=${REPO_ROOT}
worktree_path=${TARGET_WORKTREE}
EOF
