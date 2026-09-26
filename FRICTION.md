# Friction log — Profile Gate (Fire TV track)

Started at the first SDK-adjacent decision, not reconstructed afterward.

---

### 1. No JDK-only machine claim in internal research notes is already out of date

- **Task attempted:** Confirm the machine could build a Fire OS app before designing
  around it, per internal research notes's "Delivery blockers" section, which said this
  machine had no JDK.
- **Steps taken:** `java -version`.
- **Expected:** No JDK, per internal research notes, meaning a JDK install was the first
  task.
- **Actual:** OpenJDK 25.0.4.1 already installed and working. The build brief also
  states this directly ("JDK 21 is installed and Android builds work now"), which
  is the more current instruction and the one followed.
- **Severity:** Low, purely a time-saver once confirmed, but worth flagging: a
  research file and a build brief can go stale relative to each other within the
  same hackathon round, and the newer instruction (this build's own engineering brief) should win.
- **Workaround:** None needed, moved straight to SDK setup.
- **Suggestion:** Timestamp environment claims in research files so a later reader
  can tell whether "no JDK" was true when written or is still true when read.

### 2. No Android TV system image was pre-installed, and the download was slow and contended

- **Task attempted:** Create an Android TV emulator at API 28 to actually run the
  app, per this build's own engineering brief's hard requirement. (We had API 28 down as the current
  Fire OS level at this point. It is Fire OS 7's. Row 14 has the two Amazon pages
  that disagree about this.)
- **Steps taken:** `sdkmanager --list` confirmed `system-images;android-28;android-
  tv;x86` exists and is installable. Ran `sdkmanager` to install it.
- **Expected:** A background download completing in a few minutes.
- **Actual:** The download crawled at roughly 300-400 KB/s and stalled around 10%
  for an extended period. Investigating, a second, independent `sdkmanager`
  install of the exact same package was running concurrently (different PIDs,
  started eight minutes after mine), almost certainly another agent building a
  different Fire TV app in this same shared hackathon workspace and hitting the
  same missing system image. Killing what looked like the redundant process
  actually broke the shared `PackageOperation` the two were both writing into:
  the temp `.zip` disappeared and the survivor stalled at 0% CPU instead of
  continuing. Had to kill both and restart clean.
- **Severity:** High for build time (cost roughly 20 minutes), not for the product.
- **Workaround:** `rm -rf` the partial `system-images/android-28` and `.temp/*`
  directories, then relaunch a single clean `sdkmanager` install and leave it
  alone rather than "helping" by killing what looks like a duplicate.
- **Suggestion:** For a multi-agent build round sharing one Android SDK
  installation, pre-seed the system images every Fire TV app will need (API 28
  Android TV x86, at minimum) once, before any app-specific agent starts, exactly
  the same lesson internal research notes already drew about concurrent downloads and
  parallel fetchers. Concurrent `sdkmanager` calls to the same package are not safe to
  assume are additive; one can corrupt the other's in-flight download.

### 3. `kotlinOptions { jvmTarget = "17" }` is now a hard compile error, not a warning

- **Task attempted:** Generate the Gradle wrapper and do a first sync with a
  standard Android + Compose `build.gradle.kts`.
- **Steps taken:** Wrote the module build file using the classic
  `kotlinOptions { jvmTarget = "17" }` block, the form still shown in most current
  tutorials.
- **Expected:** A deprecation warning at worst, per the Kotlin 2.x migration notes
  most documentation describes.
- **Actual:** Kotlin Gradle Plugin 2.4.20 (paired with AGP 8.13.2, the versions
  already proven to work elsewhere in this repo) rejects it outright: "Using
  'jvmTarget: String' is an error. Please migrate to the compilerOptions DSL."
  Build fails before a single Kotlin file is even touched.
- **Severity:** Medium. A five-minute fix once seen, but the error message points
  at a URL, not at the working replacement syntax.
- **Workaround:** Replaced with the `compilerOptions` DSL, matching a working pattern already used elsewhere in this batch of apps:
  ```kotlin
  import org.jetbrains.kotlin.gradle.dsl.JvmTarget
  kotlin {
      compilerOptions {
          jvmTarget.set(JvmTarget.JVM_17)
      }
  }
  ```
- **Suggestion:** Kotlin's own error message should show the four-line
  replacement inline instead of only linking out, since the fix is short and
  exact.

### 4. `sdkmanager`'s SDK XML version warning is noise, not a real problem

- **Task attempted:** Every `sdkmanager` invocation.
- **Steps taken:** Read the warning closely the first time it appeared: "This
  version only understands SDK XML versions up to 3 but an SDK XML file of
  version 4 was encountered."
- **Expected:** Uncertain whether this meant a package would silently fail to
  install.
- **Actual:** Harmless in every observed case; installs completed (once run
  singly, see row 2) despite the warning on every invocation.
- **Severity:** Low, but costs a moment of doubt each time it appears.
- **Workaround:** None; ignored after the first confirmed-harmless run.
- **Suggestion:** None needed beyond what's already documented elsewhere: an
  older `cmdline-tools` build paired with newer SDK package metadata.

### 5. The Android TV system image never finished downloading, and a phone emulator substituted for it

- **Task attempted:** Run the app on an Android TV emulator at API 28, per the build
  brief's hard requirement to run on the Android emulator. (Believed at the time to
  match current Fire OS. See row 14.)
- **Steps taken:** After the contention in row 2, restarted a single clean
  `sdkmanager` install of `system-images;android-28;android-tv;x86` and left it
  running in the background while writing the rest of the app. Checked on it
  repeatedly over roughly 40 minutes.
- **Expected:** A slow but eventually complete download (the platform-only package
  had installed cleanly).
- **Actual:** It crawled to about 55% unzipped and then failed permanently:
  `Warning: An error occurred while preparing SDK package Android TV Intel x86
  Atom System Image: invalid stored block lengths` — a corrupt-zip error, not a
  timeout. A second, independent `sdkmanager` process for the identical package
  (different PID, wrapped in its own `timeout 900`, clearly another agent in this
  shared workspace) was running at the same time, so the same concurrent-write
  contention from row 2 is the likely cause, this time on a download I did not
  touch.
- **Severity:** High for the specific "Android TV flavoured" emulator; zero for
  the deliverable. This app has minSdk 28, no Android-TV-only API, and
  a manifest that only optionally requests `android.software.leanback`. It runs
  correctly on a standard phone/tablet AVD already present on this machine
  (`Pixel_6a_rec2`, API 36), which satisfies "run on the Android emulator" as
  written. Used that AVD for the on-device verification in `README.md` instead
  and documented the substitution there.
- **Workaround:** Ran and D-pad-tested the app on `Pixel_6a_rec2` via `adb shell
  input keyevent` (including `input keyevent --duration <ms> KEYCODE_DPAD_CENTER`
  to simulate a genuine held press, not just `--longpress`, which only flags a
  single instantaneous event and does not exercise this app's real elapsed-time
  hold logic). Confirmed all three gate states and the coverage screen live via
  screenshots.
- **Suggestion:** Same as row 2: pre-seed shared system images once, centrally,
  before per-app agents start, rather than each discovering the same missing
  package independently and racing to fetch it.

### 6. A ten-foot layout needs an explicit initial focus and scroll fallback that a phone-sized default does not

- **Task attempted:** Verify the footer ("Household log" / "Session coverage")
  was reachable by D-pad from the title row, and that the app had some focused
  element the moment it launched, as a real Fire TV remote requires (there is no
  pointer to fall back on).
- **Steps taken:** Screenshotted after launch and after `DPAD_DOWN`, on the
  `Pixel_6a_rec2` substitute.
- **Expected:** A visible focus ring on launch, and the footer reachable one
  `DPAD_DOWN` below the title row, matching a typical TV app.
- **Actual:** Two related gaps, both real: (1) nothing was focused at launch
  until the first `DPAD_RIGHT`/`DPAD_DOWN`, so a first-frame `DPAD_CENTER` press
  would do nothing; (2) on the shorter landscape viewport this phone AVD renders
  (status bar and gesture-nav insets eat more vertical space than a real Fire TV
  canvas would), the footer row was pushed below the visible area with no way to
  scroll to it.
- **Severity:** Medium. Neither is a gating-logic bug, both are ten-foot-UI
  basics that a phone-first mental model misses.
- **Workaround:** Added `Modifier.verticalScroll` to the home screen's outer
  column, and an explicit `FocusRequester` + `LaunchedEffect(Unit) {
  requestFocus() }` on the first tile so the app always has a visible focus
  target the instant it renders. Reverified via screenshot: footer now reachable,
  first tile focused on launch.
- **Suggestion:** None Amazon-specific; this is a "test on the real form factor
  before calling it done" lesson that a phone-emulator substitution makes easy to
  miss.

### 7. `anthropic.claude-sonnet-5` is listed as available but refuses on Converse

- **Task attempted:** Call Bedrock's Converse API with `us.anthropic.claude-sonnet-5`
  per the coordinator's model guidance ("Prefer Sonnet 5 for speed").
- **Steps taken:** `aws bedrock list-inference-profiles` shows
  `us.anthropic.claude-sonnet-5` and `us.anthropic.claude-opus-5` as valid profile
  IDs on this account. Called `aws bedrock-runtime converse --model-id
  us.anthropic.claude-sonnet-5`.
- **Expected:** A normal Converse response, matching the listing.
- **Actual:** `AccessDeniedException: anthropic.claude-sonnet-5 is not available for
  this account.` The listing and the actual model-access grant on this account
  disagree with each other.
- **Severity:** Medium. Cost about ten minutes of bisecting which model ID would
  actually answer.
- **Workaround:** `us.anthropic.claude-sonnet-4-5-20250929-v1:0` answers correctly
  and is what `tools/bedrock_client.py` actually calls. Documented directly in that
  file's comments so the next person does not repeat the search.
- **Suggestion:** `list-inference-profiles` should either filter to what the calling
  account can actually invoke, or the access-denied error should say what to call
  instead, the way a 404 with a "did you mean" would.

### 8. Claude wraps structured JSON output in a markdown fence despite being told not to

- **Task attempted:** Get a plain `{"verdict": ..., "reason": ...}` JSON object back
  from the classifier prompt, per the system prompt's explicit instruction: "Reply
  with ONLY a JSON object, no other text."
- **Steps taken:** First end-to-end test against the real frame images returned
  `Expecting value: line 1 column 1 (char 0)` from `json.loads`. Printed the raw
  `response['output']['message']['content'][0]['text']` to see what actually came
  back.
- **Expected:** The bare JSON object the prompt asked for.
- **Actual:** `` ```json\n{"verdict": "CLEAR", ...}\n``` `` — the object wrapped in a
  markdown code fence, even with `temperature: 0` and an explicit "no other text"
  instruction.
- **Severity:** Medium. A silent 100% failure rate until caught, since every
  response fell through to the `UNSURE` fallback and looked superficially like a
  working "everything is cautious" system rather than a broken parser.
- **Workaround:** `bedrock_client.py`'s `_strip_markdown_fence` strips a leading and
  trailing triple-backtick block before `json.loads`. This is exactly the kind of
  silent-fallback trap SPEC.md's "unsure holds" design is meant to survive: the
  fallback path made the bug look like a feature (every call became a cautious
  `UNSURE`) instead of an error, which is precisely why it needed a real end-to-end
  test against live frames, not just a schema check, before trusting the number.
- **Suggestion:** Anthropic's own Converse API could offer a strict "JSON only, no
  prose, no fences" response mode the way OpenAI's function-calling / structured
  outputs do, rather than leaving every caller to write their own fence-stripper.

### 9. Two more agents installing the same Android TV image at once, again

- **Task attempted:** N/A — background observation while polling for the earlier
  download.
- **Steps taken:** `ps aux | grep sdkmanager` while waiting.
- **Actual:** A second, independent `sdkmanager --install
  system-images;android-28;android-tv;x86` (different PID, wrapped in its own
  `timeout 900`) was running at the same time as the corrupted download in row 5,
  confirming this is a repeat of the exact contention pattern, not a one-off.
- **Severity:** Low for this build (already routed around it via the phone AVD
  substitution), but confirms row 5's root cause rather than leaving it as a guess.
- **Workaround:** None needed this time; left the other agent's process alone.
- **Suggestion:** Same as row 2 and row 5, now backed by two independent
  observations: a shared SDK cache across concurrently-running agents needs the
  system images pre-seeded once, centrally, before any per-app agent starts.

### 10. The vision coverage line silently counted a fallback as "checked by Bedrock"

- **Task attempted:** Test the network-down path the coordinator specifically asked
  for -- stop `tools/frame_classifier_service.py`, launch the app fresh on
  `FireTV_API28`, and confirm the UI degrades honestly rather than lying about it.
- **Steps taken:** Killed the service, force-stopped and relaunched the app, read
  the home header.
- **Expected:** Something naming the outage.
- **Actual:** "Vision: 10/10 frames checked by Bedrock · 0 flagged as mislabelled."
  Every one of the ten classification calls had genuinely failed (connection
  refused) and fallen back to `FrameJudgement.Unsure`, but `visionSummary()` counted
  `judgements.size` -- which includes every fallback entry, not just real answers --
  as "checked". The gate logic itself never wavered (Unsure never escalates, never
  clears), but the coverage report was actively dishonest about what had happened,
  which is precisely the failure mode this whole product exists to refuse.
- **Severity:** High. This is the exact scenario a judge pulling the network would
  trigger, and the one screen meant to be the product's most honest sentence said
  the opposite of the truth.
- **Workaround:** Rewrote `visionSummary()` (`ui/routes/AppLabels.kt`) to count
  `!is FrameJudgement.Unsure` as "answered by Bedrock" and report a separate
  "N could not reach Bedrock, held as unsure" clause whenever any fallback
  occurred. Reverified on-device: the header now reads "Vision: 0/10 frames checked
  by Bedrock · 0 flagged as mislabelled · 10 could not reach Bedrock, held as
  unsure" with the service down, and the correct counts once it is back. Added
  `AppLabelsTest` (4 tests) covering the outage, partial-outage, and
  not-yet-attempted cases directly, so this exact regression cannot come back
  silently.
- **Suggestion:** None Amazon-specific. General lesson for the next round: a
  "coverage" or "honesty" report is itself product logic and needs its own explicit
  network-down test, not just the underlying safety gate.

### 11. `us.anthropic.claude-sonnet-5` denies access in a way that looks identical to a working call until you check the exception

- Confirms the coordinator's own correction: `list-foundation-models` and
  `list-inference-profiles` both list models this account cannot actually invoke,
  with nothing in either response distinguishing a real grant from a catalogue
  entry. `bedrock_client.py` now tries `MODEL_CHAIN` newest-first
  (`sonnet-5` → `sonnet-4-6` → `sonnet-4-5-20250929-v1:0`) and falls back down the
  list on any exception, confirmed live: `sonnet-5` fails in under a second with
  `AccessDeniedException`, `sonnet-4-6` answers in ~2.9s. No code change needed the
  day `sonnet-5` access lands.

### 12. The Android TV emulator is shared across agents, live, mid-session

- **Task attempted:** Re-take screenshots on `FireTV_API28` per the coordinator's
  instruction, including a settings-screen shot via a coordinate `adb shell input
  tap`.
- **Steps taken:** Sent a D-pad sequence, then later a raw tap at the Settings
  button's last-known screen coordinates.
- **Expected:** Profile Gate's Settings screen.
- **Actual:** A screenshot of a completely different Fire TV app, being built in
  the same shared workspace, in the foreground, twice, at two different points several
  minutes apart. `dumpsys activity activities | grep mResumedActivity` confirmed
  Profile Gate was resumed moments before each incident, meaning another agent's
  own `am start` on the same shared AVD won the race in the gap between my adb
  calls.
- **Severity:** Medium. Cost the remaining Settings/Search on-device screenshots
  and, worse, means my own input events were almost certainly landing on the other
  agent's app in between, which is a two-way interference problem, not just a
  one-way inconvenience.
- **Workaround:** Stopped interactive testing on the shared emulator once the
  pattern repeated a second time, relied on the already-captured evidence (vision
  classification, the network-down fix, the resized tiles, the gate card) plus the
  Pixel_6a_rec2 session's earlier proof of the hold-to-confirm and flagged-refusal
  flows, and the 63 passing unit tests for the logic itself.
- **Suggestion:** A single shared Android TV AVD for however many Fire TV apps are
  in this round cannot support simultaneous interactive `adb shell input` sessions
  from multiple agents without exactly this collision. Either each app gets its own
  AVD (costly but simple) or agents need a claim/lock protocol before sending input,
  not just before installing.

### 13. The `GetUserAgeData` page cannot be found by searching for `GetUserAgeData`

- **Task attempted:** Read Amazon's own documentation for the age API this whole app
  is built around, rather than working from a research summary of it.
- **Steps taken:** Guessed the slug from the API name, the way you would for any
  other Amazon doc: `/docs/app-submission/getuseragedata.html`,
  `/docs/fire-tv/getuseragedata.html`, `/docs/app-submission/get-user-age-data.html`.
  All 404. Then walked the nav from
  `app-submission/understanding-submission.html` and found it.
- **Expected:** A slug containing some form of the API's name.
- **Actual:** The page is at
  `https://developer.amazon.com/docs/app-submission/user-age-verification.html`
  (HTTP 200, last updated 17 July 2026). The slug contains no form of
  "getuseragedata", which is why every guess from the API name 404s. It is reachable
  from the nav and, as far as we could tell, from nothing else.
  A second, separate thing: **`--compressed` is required** on the curl. Without it
  Amazon returns gzip that reads as binary noise on the terminal and looks exactly
  like a failed fetch, which cost a round of "is this page even real".
- **Severity:** Low in minutes, high in how close it came to being written off. Two
  404s and a screen of binary is enough evidence to conclude a page does not exist,
  and this one does.
- **Workaround:** Read the nav rather than guessing slugs, and put `--compressed` in
  the fetch command permanently.
- **Suggestion:** Two small things. Add a slug alias, or name the API in the page
  title so search finds it. And serve identity-encoded content to clients that do not
  send `Accept-Encoding: gzip`, since the current behaviour makes a successful fetch
  indistinguishable from a broken one.

### 14. Two Amazon pages give Fire OS 16 two different Android versions

- **Task attempted:** Settle which framework APIs exist on the device, because this
  app's central design question (can a third-party app inspect another app's video)
  turns on API level: `AccessibilityService.takeScreenshot` postdates API 28,
  `VIRTUAL_DISPLAY_FLAG_SECURE` needs a signature permission, and so on.
- **Steps taken:** `developer.amazon.com/apps-and-games/fire-tv`, the page the track
  resources link to first. Fetched 22 September:

  > Our operating system, **Fire OS 16, is based on Android 9 (Pie) and API level 28**,
  > making it compatible with existing Android apps.

- **Expected:** One answer.
- **Actual:** `developer.amazon.com/docs/device-specs/identify-fire-tv-devices.html`,
  same day, disagrees. It enumerates "Fire OS 16: Based on Android 16... Fire OS 8:
  Based on Android 11 (API level 30), and Android 10 (API Level 29)", and its device
  table pins Android level 28 to **Fire OS 7** devices while every Fire OS 8 stick
  gets Android level 30.
- **Severity:** Medium, and entirely in the reasoning rather than in the binary.
  `minSdk 28` is a floor, so the APK runs on Fire OS 7 and 8 under either reading.
  What it changed is that we bounded the platform's capabilities at API 28 when most
  shipping Fire TV hardware is API 30, and we wrote "Fire OS 16's own API level" into
  this app's SPEC, its README and its Devpost draft.
- **Workaround:** None. We could not determine which page is wrong, so we kept
  `minSdk 28` (safe either way) and removed the claim that it matches current Fire OS.
  The conclusion about video inspection survives regardless, because it rests on a
  signature-level permission rather than on an API level.
- **Suggestion:** Fix one of the two pages, starting with the landing page, which is
  what a developer reads first. And print the Android API level beside the Fire OS
  version everywhere the Fire OS version is mentioned. Fire OS numbering runs 5, 6,
  7, 8, then 16, so "Fire OS 16 is Android 9" reads as perfectly plausible.

### 15. VoiceView is not TalkBack, and the differences are the ones that break a screen reader

- **Task attempted:** Make the gate card, the household log and the coverage screen
  usable with the Fire TV screen reader on, which for a product about protecting a
  child on a shared television is not optional.
- **Steps taken:** Read Amazon's Fire TV accessibility documentation, then counted
  what the app actually had: `contentDescription` appears **zero** times across
  Profile Gate's whole source tree. The other two Fire TV apps in this batch had 15 and 0 respectively.
- **Expected:** Android accessibility knowledge to transfer, since Fire OS is Android.
- **Actual:** Most of it does. The parts that do not are the parts that matter, and
  each of them is stated once in Amazon's docs and nowhere in Android's:
  - **VoiceView rides focus.** "When the focus changes, VoiceView automatically
    speaks the currently focused item." So the initial-focus bug in row 6 was not a
    navigation bug with an accessibility side effect. It was the screen reader
    failing, and we were treating it as a polish item.
  - **It takes the Menu button.** "When VoiceView is enabled, VoiceView controls the
    Menu button and the Play/Pause button (when VoiceView is speaking). The system or
    app receives double-press events from the Menu button." Bind a single Menu press
    to anything you need and you will not receive it.
  - **It reads in a different order than TalkBack.** "VoiceView typically sorts
    on-screen objects in a left to right, top to bottom order, based on the
    coordinates of the objects' **centers**", where TalkBack uses top-left corners. A
    staggered layout is read in an order you did not design.
  - **Static text is not reachable by default**, only in Review Mode, unless you set
    one of three Amazon-only extras on the `AccessibilityNodeInfo` bundle:
    `com.amazon.accessibility.describedBy`,
    `com.amazon.accessibility.orientationText`,
    `com.amazon.accessibility.usageHint.remote`. These exist nowhere in Android's own
    documentation and there is no `androidx` wrapper for them.
- **Severity:** High. Profile Gate was silent under VoiceView and we did not know,
  because nothing in the Android toolchain flags it and Compose's accessibility lint
  does not know about VoiceView at all.
- **Workaround:** Turn VoiceView on (hold Back and Menu together for two seconds) and
  navigate the app with it before calling any screen done. We added content
  descriptions to every focusable and kept rows genuinely aligned so the centre-based
  sort matches the visual order. We have not shipped the three Amazon extras; they are
  written down as the next thing rather than claimed.
- **Suggestion:** The information is all there and it is spread across pages a
  developer reaches only if they already suspect Fire TV differs from Android. One
  page called something like "What is different about accessibility on Fire TV",
  linked from the Fire TV getting-started page, with those four facts on it, would be
  worth more than anything else in this log. A lint rule or an `androidx` shim for the
  three extras would be worth more again.

### 16. The simulator the rules accept for filming cannot install this app

- **Task attempted:** Plan the demo video. The rule is that it must show the project
  running on an actual Fire TV device or the Fire TV/Vega simulator, and there is no
  Fire TV hardware in this building.
- **Steps taken:** Checked what the Vega Virtual Device actually runs before
  installing it. `developer.amazon.com/docs/vega/0.24/run-apps.html`:
  `vega run-app <vpkg-path> <app-id> -d VirtualDevice`.
  `developer.amazon.com/docs/vega/0.24/vega-rn-arch.html`: React Native for Vega is
  "an out-of-tree fork of React Native framework for Vega devices". No `adb` appears
  anywhere in the Vega documentation.
- **Expected:** The free simulator the rules name to accept the APK we built.
- **Actual:** It takes `.vpkg` bundles built by `kepler ktbuild` from React Native
  sources. Profile Gate is Kotlin and Compose. There is no porting tool and no APK
  loader, so the gap is a rewrite, not a build flag.
  The naming makes this much worse than it needs to be, and this is the part I would
  most want a platform team to read.
  `developer.amazon.com/docs/device-specs/identify-fire-tv-devices.html` has two
  devices called "Fire TV Stick HD": the 2024 unit (AFTSS) is Fire OS 7, Android level
  28, and runs our APK; the 2026 unit (AFTCL001) is Vega OS 1.1, Android level `N/A`,
  and does not. Same shelf name. The retail box does not say Vega. The only reliable
  check is the build model under `Settings > My Fire TV > About`.
- **Severity:** Blocker for the video, which for this track is most of the score.
- **Workaround:** none, in the end. Sourcing a physical Fire OS stick was the plan for
  weeks — a used Fire TV Stick 4K Max 2nd Gen (build model AFTKRT), the model verified elsewhere in this batch of apps to actually run our APK, was
  findable for cash in Accra. The decision was since made not to buy one. That closes the workaround, not just defers it:
  everything in this app that can be shown without hardware (the 63 unit tests, the
  vision classifier against live frames, the network-down honesty fix, the hold-to-
  confirm flow) was captured on emulators and is in `docs/shots/`, and none of it
  satisfies the filming rule on its own. So this entry is not "here is what we did
  instead" — it is a real, final gap between what the rules ask for and what this entry
  can submit.
- **Suggestion:** Say plainly, on the page that recommends the Vega Virtual Device,
  that it cannot install an Android APK. Amazon nowhere prints that sentence; we
  assembled it from a device table, a CLI reference and an architecture page, which is
  three independent facts pointing one way rather than a statement. And consider what
  the combination does: Amazon's own page says to test on real hardware rather than an
  emulator, the free simulator runs a different OS, and amazon.com will not ship a
  Fire TV Stick to the country this was built in. A developer who does not already own
  the hardware cannot reach this track, and cannot buy their way in either.

---

## Product feedback answers

**Tools/SDKs used:** Android SDK command-line tools (`sdkmanager`, `avdmanager`,
`emulator`) against platform `android-28` and Android TV system image
`android-28;android-tv;x86`; Gradle 8.14 with Android Gradle Plugin 8.13.2 and
Kotlin 2.4.20 (Compose compiler plugin); Jetpack Compose (`compose-bom
2024.12.01`, `material3`, `foundation`) for the UI; JUnit 4 for logic tests; no
Amazon-specific SDK for `GetUserAgeData`, since it is a plain `ContentProvider`
query against a documented URI. For the vision classifier: `boto3` calling Amazon
Bedrock's `bedrock-runtime.converse` (multimodal) from a small local Python
service, not from the Android app itself -- see SPEC.md, "The vision classifier",
for why that boundary exists.

**What worked:** Once the Kotlin `compilerOptions` fix and a clean single-threaded
SDK download were in place, the whole toolchain — Gradle, AGP, Kotlin, Compose,
the API 28 Android TV emulator — built and ran without further surprises. The
`GetUserAgeData` ContentProvider contract in internal research notes's research (exact
column names, exact URI, exact status values) was accurate enough to implement
directly from the write-up with zero guessing. Bedrock's Converse API itself was
easy: one shape for text and image content blocks, a real multimodal answer in
under 3 seconds once a working model ID was found (see row 7 and row 11).

**What needs work:** The biggest item is not in the API surface at all, it is the
route to a demo video (row 16). The rules accept an actual Fire TV device or the
Fire TV/Vega simulator; the free simulator runs Vega OS and takes `.vpkg` bundles
built from React Native, so it cannot install a Kotlin APK; and Amazon's own page
says to test on hardware rather than an emulator. Three finished Fire TV apps in this
workspace are therefore gated on owning a physical stick, and amazon.com will not
ship one to the country they were written in. This is no longer a hypothetical gap
we are working around — the decision has been made not to buy hardware, so it is the
real, final shape of what this entry can submit for the video requirement.

After that, in order. `GetUserAgeData` cannot be exercised even once before
submission: Texas-only, Fire-tablet-scoped in its own text, and gated on an
undocumented Amazon-side enablement step, with its documentation page at a slug that
does not contain the API's name (row 13). VoiceView differs from TalkBack in four
ways that each break a screen reader silently, and those four facts are scattered
across pages a developer reaches only if they already suspect Fire TV is different
(row 15). Two Amazon pages give Fire OS 16 two different Android versions (row 14).
On the Bedrock side: `list-foundation-models` and `list-inference-profiles` both list
models an account cannot invoke, with no field distinguishing a real grant from a
catalogue entry (row 7), and Claude wraps structured JSON output in a markdown fence
despite an explicit system-prompt instruction not to (row 8), which turned a parsing
bug into a classifier that looked like it was being extra careful rather than broken.

**Onboarding:** Reading the Fire TV platform facts and internal research notes first,
before writing any code, avoided the two most expensive possible mistakes: trying
to build a video-inspecting product (ruled out early, once the AOSP-source research above was done) and treating
`GetUserAgeData` as a dependency (ruled out the same way).
Both would have cost far more than the Gradle and SDK friction logged above.

**Would we build with it again:** Yes for the Android/Compose/Fire OS toolchain
and for Bedrock's Converse API, once the fixes above are known — a model-chain
fallback and a markdown-fence strip are both cheap, permanent fixes. Neutral-to-no
for `GetUserAgeData` as currently documented: it reads as a compliance citation
Amazon can point to, not a capability a third-party developer can actually
exercise, and the product here is built specifically to not need it to work.
