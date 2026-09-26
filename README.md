# Profile Gate

Fire TV age assurance for the moment a locked children's profile is about to show
something it should not, and nobody can be sure who is actually holding the remote.
See `SPEC.md` for the full design, the evidence behind it, and what this deliberately
does not do.

## What it is, in one paragraph

A federal judge approved a $10,000,000 penalty against Disney on 31 December 2025 for
mislabelling children's video, and ordered a program that decides whether a video is
child-directed unless age assurance ships first. Profile Gate builds both halves of
that sentence. A real Amazon Bedrock call (Claude, multimodal, resolved through a
model preference chain — see `tools/bedrock_client.py`) looks at
the one frame each mock title carries and says whether it matches its own declared
band — the "is this actually Made for Kids" program the order describes. Amazon's own
`GetUserAgeData` API is asked first and treated as a bonus, since it is documented for
Fire tablets, not Fire TV, and live only in Texas today. Whatever band results from
those two, the remote — the one sensor a Fire TV stick is guaranteed to have — decides
how hard a speed bump should be when someone reaches above it. A household can mark a
refusal wrong behind a PIN, and the coverage screen reports the resulting
false-positive rate as a number, never a claim of accuracy. It never inspects another
app's video. It never claims to be safe. See `SPEC.md` for the full design and the
primary sources behind it.

## What's new since the first build

The first pass had no model in it. This one does, plus the depth a real household
would need on day two:

- **A real Bedrock vision call**, not a rule, decides whether a title's frame matches
  its own label — see `SPEC.md`, "The vision classifier".
- **A correction and a measured false-positive rate**, behind a household PIN,
  record-only — see `SPEC.md`, "The correction". This was the top-ranked feature in
  an internal feature-depth review.
- **The household log survives an app restart** (`EventLogStore` /
  `SharedPreferences`), not just one session.
- **Search**, wired to the same remote-behaviour signal as everything else: using it
  nudges a presence read toward adult-like, the same as it would for a real parent
  typing a known title's name.
- **A diagnostics-only Settings screen**: the declared band, `GetUserAgeData`'s
  status, and a live "Test connection" against the Bedrock frame classifier service.
  It deliberately does not let anyone edit the declared band from here — see
  `SPEC.md` for why that would be a bypass, not a feature.

## Running it

**Two things stop a clean clone building, and neither says so clearly.**

1. **JDK 21, not newer.** Gradle 8.14 does not run on JDK 22 or above, and when
   it finds one it prints that JDK's version number as the entire error message
   — a clean-checkout check of this repo got back the complete error text
   `25.0.4.1`, which mentions neither Java nor Gradle. Set `JAVA_HOME` before
   building:

   ```bash
   export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64   # Debian/Ubuntu
   export JAVA_HOME=$(/usr/libexec/java_home -v 21)      # macOS
   ```

2. **An Android SDK location.** `local.properties` is deliberately not committed,
   because it holds an absolute path from whoever built last. Export
   `ANDROID_HOME`, or write one:

   ```bash
   echo "sdk.dir=$HOME/Android/Sdk" > local.properties
   ```

   You need platform 35 and build-tools; Android Studio's SDK Manager installs
   both.

`./build.sh` checks both and explains either failure in words rather than in a
version number. It passes its arguments straight to Gradle:

```bash
./build.sh testDebugUnitTest     # logic tests, no emulator or network needed, ~30s
./build.sh                       # builds the APK (assembleDebug)
```

For the vision classifier only, you also need a Python 3 venv with `boto3` and AWS
credentials with Bedrock access in `us-east-1` on your shell (`aws configure` — the
service holds them, the app never does). The app runs and gates correctly with no
vision service running at all — every title falls back to its declared catalogue
band, exactly as `SPEC.md` describes for `FrameJudgement.Unsure`.

```bash
# Optional, for the real Bedrock vision demo:
cd tools && python3 -m venv .venv && .venv/bin/pip install -r requirements.txt
.venv/bin/python frame_classifier_service.py   # listens on 127.0.0.1:8787
```

(`pillow` is only needed if you also want to regenerate the synthetic frames with
`tools/build_frames.py`; the classifier service itself does not decode images —
frames arrive already base64-encoded from the Android client.)

**Run and verified:** the Android-TV-flavoured system image
(`system-images;android-28;android-tv;x86`) stalled and then corrupted mid-unzip on its
first download attempt, most likely from a package conflict during the build, and
testing briefly substituted a standard phone AVD (API 36) while that was unresolved — an
honest substitution, not a skipped step. A clean, single-threaded download fixed the
system image afterward, and this app was rebuilt, installed, and D-pad-tested end to end
on the real Android TV emulator (`FireTV_API28`, Fire OS's own API 28 profile) from then
on. It has since also been built for Fire OS and tested on a real Fire TV. The app has no
Android-TV-only dependency (minSdk 28, `android.software.leanback` is declared optional),
so either an Android TV AVD or a phone AVD will run it if you want to reproduce the
earlier substitution:

```bash
emulator -list-avds                              # pick one, or create one first
$ANDROID_HOME/emulator/emulator -avd <your-avd-name> -no-snapshot -no-audio &
$ANDROID_HOME/platform-tools/adb wait-for-device
./gradlew installDebug
$ANDROID_HOME/platform-tools/adb shell am start -n com.profilegate.app/.MainActivity
```

If an Android TV AVD is available in your environment, the same steps work
against it unchanged; the app does not know or care which one it is running on.

Navigate with the D-pad (arrow keys on a keyboard connected to the emulator, or the
emulator's own D-pad control). SELECT is Enter or the emulator's centre button.

## The demo, in three minutes

1. **Home.** The header shows the declared band and whether `GetUserAgeData`
   answered or was silent (it will be silent on this build; see `SPEC.md` point 1).
   Navigate the row calmly and pick an in-band title (`Puddle Friends`,
   `Story Barn`, etc.) — nothing is asked, it plays straight through as CLEAR.
2. **Unsure, resolved.** Navigate slowly and deliberately to an above-band title
   (`Nebula Cadets: Lockdown Special`, rated 13-15, inside a 0-12 profile). The gate
   card appears: *"Is a grown-up watching? Press and hold OK."* Hold Enter/OK for
   two seconds. The ring fills, the card resolves to CLEAR, and the household log
   records `unsure -> held OK -> passed`.
3. **Flagged, refused.** Navigate rapidly with lots of direction reversals (mimicking
   a small child's input) to the same or another above-band title. The classifier
   reads `CHILD_LIKE` and the gate refuses outright: no hold control is even shown.
   The log records `flagged -> refused`.
4. **Fail closed on a missed deadline.** Reload the app (a fresh session has no
   sample history) and immediately select an above-band title before enough
   navigation has happened to classify anything. The result is UNSURE, not a silent
   pass, because a thin or absent sample window is never treated as CLEAR.
5. **Coverage.** Open "Session coverage" from the home row. It states the exact
   count of gate-eligible attempts, how many were decided inside the deadline versus
   missed it, and the resolution breakdown. It never says "safe."
6. **Vision catches a mislabelled title.** With the frame classifier service running,
   watch the home header: "Vision: N/10 frames checked by Bedrock". Once it reaches
   10, `Sunny Meadow Friends` (`t10`) — declared 0-12 like every other title in its
   row — shows "AI flagged: frame doesn't match label" and gates even though its
   catalogue band never changed. That is a real Bedrock Converse call catching a
   label its own metadata got wrong, live.
7. **Correct a flagged refusal.** After a `flagged -> refused` event (step 3), open
   the household log and press "That was me" on it. Enter the demo PIN `1234`. The
   log shows it as corrected; "Session coverage" now states a measured
   false-positive rate. The title stays refused in the log — the correction changes
   what the report says about the past event, nothing about what would play now.

## What is genuinely mocked, and where

- **The catalogue** (`CatalogRepository`) is ten invented titles with invented
  bands. No real title, studio, or Amazon catalogue data is used anywhere.
- **The per-title PNG frames** (`assets/frames/`) are synthetic shapes generated for
  this build, not real video frames — see `SPEC.md`, "Data".
- **`GetUserAgeData`** is called for real, against the real documented URI
  (`content://amzn_appstore/getUserAgeData`), through a real `ContentResolver`
  query. On the emulator, and almost certainly on a judge's machine, there is no
  Amazon Appstore provider installed, so every call resolves to `AgeSignal.Silent`
  — which is the correct, honest, expected result, not a stub standing in for a
  real answer. `AgeSignalProvider` never fabricates a response.
- **The Bedrock frame classifier is not mocked.** Every classification is a real
  network call to a real `tools/frame_classifier_service.py`, which makes a real
  `bedrock-runtime.converse` call. What is mocked is only the input (a synthetic
  PNG, never a real frame) and the fallback path (`FrameJudgement.Unsure` on any
  failure) — the model call itself is mocked at nothing but its input.
- **The household PIN** (`ui/components/PinPad.kt`) is a fixed demo value (`1234`),
  disclosed as exactly that in `SPEC.md` and in the screen's own on-screen copy —
  not a real account or credential system.
- **The behavioural classifier** (`PresenceClassifier`) is real logic running on
  real D-pad key events from the real Compose input pipeline, not a canned demo
  script. The thresholds are heuristic and openly documented as unvalidated: see
  `SPEC.md`'s "strongest argument against" section.

## Tests

`./gradlew testDebugUnitTest` runs 83 JVM unit tests, with no Android framework, no
emulator, and no network dependency — every Bedrock-adjacent test runs against a
stubbed `FrameClassifierClient` or a hand-written response string, never a live call:

- `PresenceClassifierTest` (7), `RemoteSignalTrackerTest` (7) — the remote-behaviour
  signal: the child-like / adult-like / inconclusive heuristic and the rolling
  window that feeds it, including the boundary cases.
- `GateEngineAttemptTest` (7), `GateEngineResolveTest` (3), `GateEngineContentBandTest`
  (3) — the actual safety property: an above-band title is never CLEAR on the first
  attempt for any presence reading, a missed deadline is always UNSURE, a FLAGGED
  decision cannot be resolved to CLEAR under any input, and a vision-escalated
  `contentBand` gates a title even when its own catalogue label would have cleared it.
- `VerdictParserTest` (8), `ContentJudgementEngineTest` (5) — the vision classifier's
  response parsing and the escalate-only-never-loosen rule, including a stubbed
  `FrameClassifierClient` that proves the gate logic behaves correctly with the
  Bedrock call itself stubbed out entirely.
- `EventCodecTest` (4) — the persisted-log line format round-trips, and a corrupted
  or unrecognised line decodes to `null` rather than crashing.
- `GateSessionCorrectionsTest` (3) — the correction is record-only: it sets a flag
  and nothing else, is a no-op on a non-flagged or nonexistent event.
- `AgeBandTest` (5), `CoverageStatsTest` (7) — band coverage/escalation, and the
  coverage and false-positive-rate arithmetic, including that zero flagged events
  states no rate at all rather than implying a rate of zero means anything.
- `AppLabelsTest` (4) — the exact bug a pulled network must never produce: a
  fallback silently counted as a real Bedrock answer. Caught live on the real
  Fire TV emulator with the classifier service stopped, fixed, and now pinned by
  a test for the outage, partial-outage, and not-yet-attempted cases directly.

`AgeSignalProvider` and `HttpFrameClassifierClient` wrap Android framework classes
(`ContentResolver`, `AssetManager`) and are exercised by manual QA on the emulator
(see the demo steps above), not by a JVM unit test — each would need Robolectric or
an instrumented test to mock, which this build's time budget did not include. Both
are thin try/catch-per-failure-mode wrappers with no branching complex enough to
hide a bug that manual QA would miss; the logic that matters (response parsing,
escalation, the gate itself) is exactly what the unit tests above cover.

## What it deliberately does not do

See `SPEC.md`. In short: it does not inspect video, does not depend on
`GetUserAgeData` answering, does not use a camera, does not identify a specific
person, and does not claim the behavioural read is scientifically validated.

## Licence

MIT. Third-party photograph and font credits: see [`ATTRIBUTION.md`](ATTRIBUTION.md).

```
Copyright (c) 2026 the Profile Gate contributors

Permission is hereby granted, free of charge, to any person obtaining a copy of
this software and associated documentation files (the "Software"), to deal in the
Software without restriction, including without limitation the rights to use, copy,
modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
and to permit persons to whom the Software is furnished to do so, subject to the
following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
```
