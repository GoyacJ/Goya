#!/usr/bin/env bash
set -euo pipefail

DEFAULT_WORKTREE_ROOT="/Users/goya/.codex/worktrees/goya"

usage() {
  cat <<'EOF'
Usage:
  init_thread_worktree.sh --topic <topic> [--base <branch>] [--thread-id <id>] [--worktree-root <path>] [--dry-run]

Examples:
  init_thread_worktree.sh --topic security-core
  init_thread_worktree.sh --topic oauth2-login --base springboot4.0 --thread-id 019c46a4-82e0-72f0-910d-e9e612ba221e
  init_thread_worktree.sh --topic security-core --dry-run
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

find_worktree_for_branch() {
  local branch_ref="refs/heads/$1"
  git worktree list --porcelain | awk -v target="$branch_ref" '
    $1=="worktree" { wt=$2 }
    $1=="branch" && $2==target { print wt; exit }
  '
}

TOPIC=""
BASE_BRANCH=""
THREAD_ID="${CODEX_THREAD_ID:-}"
WORKTREE_ROOT="${DEFAULT_WORKTREE_ROOT}"
DRY_RUN="false"

while [[ $# -gt 0 ]]; do
  case "$1" in
    --topic)
      [[ $# -ge 2 ]] || fail "--topic requires a value"
      TOPIC="$2"
      shift 2
      ;;
    --base)
      [[ $# -ge 2 ]] || fail "--base requires a value"
      BASE_BRANCH="$2"
      shift 2
      ;;
    --thread-id)
      [[ $# -ge 2 ]] || fail "--thread-id requires a value"
      THREAD_ID="$2"
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

[[ -n "${TOPIC}" ]] || fail "--topic is required"

REPO_ROOT="$(git rev-parse --show-toplevel 2>/dev/null || true)"
[[ -n "${REPO_ROOT}" ]] || fail "Not inside a git repository"

if [[ -z "${BASE_BRANCH}" ]]; then
  BASE_BRANCH="$(git branch --show-current 2>/dev/null || true)"
fi
[[ -n "${BASE_BRANCH}" ]] || fail "Cannot determine base branch. Use --base."

if ! git show-ref --verify --quiet "refs/heads/${BASE_BRANCH}"; then
  fail "Base branch does not exist locally: ${BASE_BRANCH}"
fi

if [[ -z "${THREAD_ID}" ]]; then
  THREAD_ID="manual-$(date +%Y%m%d%H%M%S)"
fi

THREAD_SLUG="$(slugify "${THREAD_ID}")"
TOPIC_SLUG="$(slugify "${TOPIC}")"
[[ -n "${THREAD_SLUG}" ]] || fail "Invalid thread-id: ${THREAD_ID}"
[[ -n "${TOPIC_SLUG}" ]] || fail "Invalid topic: ${TOPIC}"

THREAD_SHORT="${THREAD_SLUG:0:12}"
BRANCH_NAME="codex/${THREAD_SHORT}-${TOPIC_SLUG}"
WORKTREE_PATH="${WORKTREE_ROOT%/}/${THREAD_SHORT}-${TOPIC_SLUG}"

if [[ "${DRY_RUN}" == "true" ]]; then
  run_cmd mkdir -p "${WORKTREE_ROOT}"
else
  mkdir -p "${WORKTREE_ROOT}"
fi

if [[ -e "${WORKTREE_PATH}" ]]; then
  if git -C "${WORKTREE_PATH}" rev-parse --is-inside-work-tree >/dev/null 2>&1; then
    CURRENT_BRANCH_AT_PATH="$(git -C "${WORKTREE_PATH}" branch --show-current 2>/dev/null || true)"
    if [[ "${CURRENT_BRANCH_AT_PATH}" == "${BRANCH_NAME}" ]]; then
      cat <<EOF
[OK] Thread worktree already exists
repo_root=${REPO_ROOT}
thread_id=${THREAD_ID}
base_branch=${BASE_BRANCH}
branch=${BRANCH_NAME}
worktree_path=${WORKTREE_PATH}
next=cd "${WORKTREE_PATH}"
EOF
      exit 0
    fi
    fail "Target path exists but points to branch ${CURRENT_BRANCH_AT_PATH}, expected ${BRANCH_NAME}: ${WORKTREE_PATH}"
  fi
  fail "Target path already exists and is not a git worktree: ${WORKTREE_PATH}"
fi

EXISTING_BRANCH_PATH="$(find_worktree_for_branch "${BRANCH_NAME}")"

if [[ -n "${EXISTING_BRANCH_PATH}" ]]; then
  if [[ "${EXISTING_BRANCH_PATH}" == "${WORKTREE_PATH}" ]]; then
    cat <<EOF
[OK] Thread worktree already exists
repo_root=${REPO_ROOT}
thread_id=${THREAD_ID}
base_branch=${BASE_BRANCH}
branch=${BRANCH_NAME}
worktree_path=${WORKTREE_PATH}
next=cd "${WORKTREE_PATH}"
EOF
    exit 0
  fi
  run_cmd git worktree move "${EXISTING_BRANCH_PATH}" "${WORKTREE_PATH}"
else
  if git show-ref --verify --quiet "refs/heads/${BRANCH_NAME}"; then
    run_cmd git worktree add "${WORKTREE_PATH}" "${BRANCH_NAME}"
  else
    run_cmd git worktree add -b "${BRANCH_NAME}" "${WORKTREE_PATH}" "${BASE_BRANCH}"
  fi
fi

cat <<EOF
[OK] Thread worktree is ready
repo_root=${REPO_ROOT}
thread_id=${THREAD_ID}
base_branch=${BASE_BRANCH}
branch=${BRANCH_NAME}
worktree_path=${WORKTREE_PATH}
next=cd "${WORKTREE_PATH}"
EOF
