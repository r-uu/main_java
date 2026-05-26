#!/usr/bin/env bash
set -euo pipefail

# IntelliJ WSL watchdog:
# - checks whether IntelliJ runs with stable WSLg env vars
# - optionally restarts IntelliJ with those vars if they are missing

required_keys=(
  "GDK_BACKEND"
  "_JAVA_AWT_WM_NONREPARENTING"
  "LIBGL_ALWAYS_SOFTWARE"
  "MESA_LOADER_DRIVER_OVERRIDE"
)

required_values=(
  "x11"
  "1"
  "1"
  "softpipe"
)

find_idea_bin() {
  local default_bin="/home/r-uu/.local/share/JetBrains/Toolbox/apps/intellij-idea/bin/idea"
  if [[ -x "$default_bin" ]]; then
    printf '%s\n' "$default_bin"
    return 0
  fi

  local candidate
  candidate=$(compgen -G "/home/r-uu/.local/share/JetBrains/Toolbox/apps/intellij-idea*/bin/idea" | head -n1 || true)
  if [[ -n "$candidate" && -x "$candidate" ]]; then
    printf '%s\n' "$candidate"
    return 0
  fi

  return 1
}

idea_pid() {
  pgrep -f '/JetBrains/Toolbox/apps/intellij-idea.*/bin/idea' | head -n1 || true
}

read_env_for_pid() {
  local pid="$1"
  tr '\0' '\n' < "/proc/$pid/environ"
}

missing_or_wrong_env() {
  local pid="$1"
  local env_dump
  env_dump=$(read_env_for_pid "$pid")

  local i
  for i in "${!required_keys[@]}"; do
    local key="${required_keys[$i]}"
    local expected="${required_values[$i]}"
    local actual
    actual=$(printf '%s\n' "$env_dump" | grep -E "^${key}=" | head -n1 | cut -d= -f2- || true)
    if [[ "$actual" != "$expected" ]]; then
      printf '%s expected=%s actual=%s\n' "$key" "$expected" "${actual:-<unset>}"
    fi
  done
}

start_idea_fixed() {
  local idea_bin
  idea_bin=$(find_idea_bin)

  rm -f /run/user/1000/jb.station.ij.*.sock 2>/dev/null || true

  DISPLAY=:0 WAYLAND_DISPLAY=wayland-0 XDG_RUNTIME_DIR=/run/user/1000 \
    GDK_BACKEND=x11 _JAVA_AWT_WM_NONREPARENTING=1 \
    LIBGL_ALWAYS_SOFTWARE=1 MESA_LOADER_DRIVER_OVERRIDE=softpipe \
    nohup "$idea_bin" >/tmp/idea-wsl-start.log 2>&1 &

  echo "IntelliJ started with stable WSLg env (PID $!)."
}

print_usage() {
  cat <<'USAGE'
Usage:
  intellij-watchdog.sh             # check running IntelliJ env
  intellij-watchdog.sh --fix       # restart IntelliJ if env is not compliant
  intellij-watchdog.sh --start     # start IntelliJ with stable env
USAGE
}

main() {
  local mode="check"
  if [[ $# -gt 0 ]]; then
    case "$1" in
      --fix) mode="fix" ;;
      --start) mode="start" ;;
      -h|--help) print_usage; exit 0 ;;
      *)
        print_usage
        exit 2
        ;;
    esac
  fi

  if [[ "$mode" == "start" ]]; then
    start_idea_fixed
    exit 0
  fi

  local pid
  pid=$(idea_pid)
  if [[ -z "$pid" ]]; then
    echo "No running IntelliJ process found."
    echo "Run with --start to launch IntelliJ with stable env."
    exit 1
  fi

  mapfile -t mismatches < <(missing_or_wrong_env "$pid")

  if [[ ${#mismatches[@]} -eq 0 ]]; then
    echo "OK: IntelliJ PID $pid has all required env vars."
    exit 0
  fi

  echo "WARN: IntelliJ PID $pid has env mismatches:"
  printf '  - %s\n' "${mismatches[@]}"

  if [[ "$mode" == "fix" ]]; then
    echo "Restarting IntelliJ with stable env..."
    kill "$pid" || true
    sleep 1
    start_idea_fixed
    exit 0
  fi

  echo "Run with --fix to auto-restart IntelliJ with corrected env."
  exit 3
}

main "$@"

