#!/usr/bin/env bash
#
# Builds Profile Gate, after checking the two things that stop a clean clone
# building and saying so in words.
#
# Both failures are silent in a bad way. Gradle 8.14 cannot run on JDK 22 or
# newer and reports it by printing the version number of the JDK it found and
# nothing else — a clean-checkout check got back the entire error text
# "25.0.4.1", which says nothing about JDKs, versions or what to do. And with
# no `local.properties` (correctly gitignored, because it holds a path from
# whoever built last) the Android plugin fails on a missing SDK location.
#
# Usage:
#   ./build.sh                 # assembleDebug
#   ./build.sh testDebugUnitTest installDebug    # or any Gradle tasks
#
set -euo pipefail
cd "$(dirname "$0")"

need_jdk() {
    cat >&2 <<'MSG'
Profile Gate needs JDK 21. Gradle 8.14 does not run on JDK 22 or newer, and
reports that by printing a version number and nothing else.

Point JAVA_HOME at a 21 and try again, for example:

    export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
    ./build.sh

On Debian or Ubuntu: sudo apt install openjdk-21-jdk
On macOS with Homebrew: brew install openjdk@21
MSG
    exit 1
}

if [ -z "${JAVA_HOME:-}" ]; then
    for candidate in /usr/lib/jvm/java-21-openjdk-* /usr/lib/jvm/temurin-21-* \
                     /Library/Java/JavaVirtualMachines/*21*/Contents/Home; do
        [ -x "$candidate/bin/javac" ] && export JAVA_HOME="$candidate" && break
    done
fi
[ -n "${JAVA_HOME:-}" ] && [ -x "$JAVA_HOME/bin/javac" ] || need_jdk

java_major="$("$JAVA_HOME/bin/javac" -version 2>&1 | sed -E 's/javac ([0-9]+).*/\1/')"
[ "$java_major" = "21" ] || need_jdk

if [ ! -f local.properties ] && [ -z "${ANDROID_HOME:-}" ] && [ -z "${ANDROID_SDK_ROOT:-}" ]; then
    cat >&2 <<'MSG'
No Android SDK location. local.properties is deliberately not committed,
because it holds an absolute path from whoever built last.

Either export ANDROID_HOME, or write the path into local.properties:

    echo "sdk.dir=$HOME/Android/Sdk" > local.properties

The build needs platform 35 and build-tools; Android Studio's SDK Manager
installs both, or:

    sdkmanager "platforms;android-35" "build-tools;35.0.0"
MSG
    exit 1
fi

echo "JDK:         $JAVA_HOME ($java_major)"
echo "Android SDK: ${ANDROID_HOME:-$(grep -s '^sdk.dir=' local.properties | cut -d= -f2-)}"
exec ./gradlew "${@:-assembleDebug}"
