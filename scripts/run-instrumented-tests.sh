#!/usr/bin/env bash
# Runs only on a runner with the Android SDK and KVM available.
set -euo pipefail

taskApiLevel="${1:?Usage: bash scripts/run-instrumented-tests.sh API_LEVEL}"
[[ "$taskApiLevel" =~ ^[0-9]+$ ]] || { echo "API_LEVEL must be numeric" >&2; exit 2; }
taskSdkRoot="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-/usr/local/lib/android/sdk}}"
for taskToolsDir in "$taskSdkRoot"/cmdline-tools/*/bin; do
    PATH="$taskToolsDir:$PATH"
done
export PATH="$taskSdkRoot/emulator:$taskSdkRoot/platform-tools:$PATH"
taskImage="system-images;android-$taskApiLevel;google_apis;x86_64"
taskAvd="biat-ci-$taskApiLevel"
taskSerial="emulator-5554"

# yes exits on SIGPIPE when sdkmanager has consumed its input.
set +o pipefail
yes | sdkmanager --licenses > /dev/null
set -o pipefail
sdkmanager "platforms;android-37.0" "emulator" "$taskImage"
printf 'no\n' | avdmanager create avd --force --name "$taskAvd" --package "$taskImage" --device "pixel_2"
if adb -s "$taskSerial" get-state > /dev/null 2>&1; then
    echo "Emulator port 5554 is already in use" >&2
    exit 1
fi
mkdir -p build
emulator -avd "$taskAvd" -port 5554 -no-window -no-audio -no-boot-anim \
    -no-snapshot -gpu swiftshader -accel on -memory 2048 -cores 2 \
    > "build/emulator-$taskApiLevel.log" 2>&1 &
taskEmulatorPid=$!
trap 'adb -s "$taskSerial" emu kill > /dev/null 2>&1 || true; kill "$taskEmulatorPid" > /dev/null 2>&1 || true' EXIT

taskBooted=false
for taskAttempt in {1..180}; do
    if [[ "$(adb -s "$taskSerial" shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" == "1" ]]; then
        taskBooted=true
        break
    fi
    kill -0 "$taskEmulatorPid" || { cat "build/emulator-$taskApiLevel.log"; exit 1; }
    sleep 1
done
[[ "$taskBooted" == true ]] || { cat "build/emulator-$taskApiLevel.log"; exit 1; }
adb -s "$taskSerial" shell input keyevent 82
adb -s "$taskSerial" shell settings put global window_animation_scale 0
adb -s "$taskSerial" shell settings put global transition_animation_scale 0
adb -s "$taskSerial" shell settings put global animator_duration_scale 0
./gradlew :biat-ui:connectedDebugAndroidTest
