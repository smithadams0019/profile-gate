#!/usr/bin/env bash
#
# Screenshots Profile Gate, after checking Profile Gate is actually what is on
# screen.
#
# Three agents share one Fire TV emulator in this workspace and all three
# install and launch their own app on it, so a `screencap` taken on faith
# captures whichever app happened to be foreground — which has already
# happened once here, producing a Profile Gate screenshot of somebody else's
# onboarding screen. This waits for the focused window to belong to us, and
# fails loudly rather than saving a picture of the wrong app.
#
# Usage: tools/shot.sh <output.png> [keyevents...]
#   tools/shot.sh docs/shots/A-home.png
#   tools/shot.sh docs/shots/B-focus.png DPAD_RIGHT DPAD_RIGHT
#
set -euo pipefail
cd "$(dirname "$0")/.."

ADB="${ANDROID_HOME:-$HOME/Android/Sdk}/platform-tools/adb"
SERIAL="${SERIAL:-emulator-5556}"
PKG=com.profilegate.app
OUT="$1"; shift || true

focused() { "$ADB" -s "$SERIAL" shell dumpsys window 2>/dev/null | grep -m1 -E "mCurrentFocus|mFocusedApp" || true; }

if ! focused | grep -q "$PKG"; then
    "$ADB" -s "$SERIAL" shell am start -n "$PKG/.MainActivity" > /dev/null
    for _ in $(seq 1 30); do
        focused | grep -q "$PKG" && break
        sleep 1
    done
fi
focused | grep -q "$PKG" || { echo "not foreground: $(focused)" >&2; exit 1; }

for key in "$@"; do
    "$ADB" -s "$SERIAL" shell input keyevent "$key"
    sleep 0.45
done
sleep 1
mkdir -p "$(dirname "$OUT")"
"$ADB" -s "$SERIAL" exec-out screencap -p > "$OUT"
echo "$OUT"
