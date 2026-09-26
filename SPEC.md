# Profile Gate

Fire TV age assurance for the moment a locked children's profile is about to show
something it should not, and nobody can be sure who is actually holding the remote.

## Who this is for

A parent who set up a Kids profile, chose an age band, and left the room. The profile
is still locked. The PIN is still set. None of that tells anyone whether the person
about to press OK on a title outside that band is the four-year-old the profile was
built for, an older sibling, or an adult who picked the wrong profile up by accident.
Every incumbent (Netflix, YouTube Kids, Fire TV itself) checks the profile once at
login and never asks again. See internal research into Fire TV family-profile and age-signal behaviour section 5
for the primary-source check on all three.

## The evidence

On 31 December 2025 a federal judge approved a **$10,000,000** civil penalty against
Disney Worldwide Services and Disney Entertainment Operations for mislabelling videos
as "Made for Kids" on YouTube, and ordered Disney to build a program that reviews
whether a video should carry that label — unless YouTube ships "age assurance
technologies that can determine the age, age range, or age category" of its viewers
first. A federal court had to order a studio to build the age-assurance half of that
sentence because nobody has shipped it. Profile Gate is that half, built for the one
box in the house where it is needed most: the television nobody is standing next to.

Full citation trail for the 2019 Google/YouTube $170m COPPA settlement, the 2022
Epic $520m order, and the Amazon Alexa $25m order was gathered from primary sources
during this build's research.

## What it does

1. **On launch, it asks Amazon's own answer first, and only once.** It queries
   `content://amzn_appstore/getUserAgeData` for the active profile. If Amazon answers
   `SUPERVISED` with an age band, that band is authoritative for the rest of the
   session and no behavioural signal is ever consulted. This will almost never
   happen on this build: the API is live only for eligible users in Texas, its own
   documentation scopes it to Fire tablets rather than Fire TV, and it returns
   `FEATURE_NOT_SUPPORTED` unless Amazon has separately enabled the calling app, with
   no documented way to request that. Profile Gate treats every other response —
   `UNKNOWN`, `CONSENT_NOT_GRANTED`, empty, an exception, or `FEATURE_NOT_SUPPORTED`
   — as **silence**, not as a signal of any kind, and logs which of those it got.
2. **The declared band still governs the catalogue.** A small on-device demo
   catalogue (clearly mock data, see Data below) carries an age band per title. A
   title whose band exceeds the profile's declared band is never played outright,
   full stop, regardless of who is holding the remote.
3. **The remote is the working signal for what happens next.** When someone reaches
   for a title above the declared band, Profile Gate is not deciding whether to show
   it — the band already said no. It is deciding how hard to make the exception a
   grown-up can still choose to make. It reads the one sensor a Fire TV stick is
   guaranteed to have: D-pad press cadence, hold duration, dwell time on a tile
   before commit, directional overshoot (reversing without landing), and whether
   search was used to get there at all. From a short rolling window of that input it
   produces one of three states — never two:
   - **Clear** — routine in-band navigation. No gate. Nothing is asked.
   - **Unsure** — reaching for above-band content with an inconclusive or
     insufficient read on who is present. One card: *"Is a grown-up watching?
     Press and hold OK."* A child can physically press it. It is a speed bump and a
     logged event, not a lock, and it says so in those words.
   - **Flagged** — reaching for above-band content with a remote-input pattern that
     matches the declared child band strongly enough that the hold-confirm is
     refused outright. The screen explains why in plain language and returns to the
     last in-band tile. This is the harder stop: evidence the request likely came
     from the child the profile was built for, not an adult making a deliberate
     exception.
4. **Unsure holds. It never guesses into a pass.** If the sample window is too thin
   to decide within a fixed budget, or if the deadline for a decision is missed, the
   state is `unsure`, never `clear`. A classifier that silently degrades to a pass
   under load is the exact failure mode this product exists to avoid.
5. **Behaviour can only restrict, never unlock.** No behavioural reading, however
   confident, can play a title above the declared band without the person actually
   completing the hold-confirm. The worst a false read can do is make an adult hold a
   button for two seconds longer than they expected. It can never expose a child to
   something the profile band excluded.
6. **It reports its own coverage, out loud, every session.** The end-of-session card
   states how many gate-eligible attempts occurred, how many were classified inside
   the decision deadline versus how many missed it and failed closed, and how many
   resolved clear, unsure-passed, unsure-held, or flagged. It never says "safe."
7. **A real model decides whether each title's own label is true.** This is the
   half of the Disney order this build could not honestly skip: "establish and
   implement a program to review whether videos... should be designated as Made for
   Kids." At launch, Profile Gate sends the one frame each mock title actually
   carries to Amazon Bedrock's multimodal Converse API (`claude-sonnet-4-5`,
   `us-east-1`) with the title's own catalogue band, and asks whether the frame is
   consistent with that band. A `FLAGGED` verdict escalates that title's effective
   band by one step — never down, only up — before the gate in points 2-5 ever
   runs. An `UNSURE` verdict (timeout, unreachable service, unparsable answer)
   changes nothing: the title keeps its declared band, exactly as if vision had
   never been asked. See "The vision classifier" below for the full design and
   why the call happens behind a local HTTP boundary, never inside the APK.

## The one screen that carries the demo

The **gate card** itself: a title above the declared band is selected, the screen
narrows to a single card naming what is being asked and why, and the three-minute
demo walks all three states back to back — an adult-paced hold that passes as
`unsure`, a child-paced attempt that gets `flagged` and refused, and one run where the
sample window is deliberately starved to show a missed deadline fail closed to
`unsure` rather than quietly passing. The end-of-session **coverage card** closes the
demo: exact counts, no claim of safety.

## The vision classifier

Every title in the mock catalogue carries one PNG frame under
`app/src/main/assets/frames/`, generated for this build and obviously synthetic
(simple flat shapes, not photographs — see Data below). At launch Profile Gate
sends each frame to a local frame classifier service
(`tools/frame_classifier_service.py`), which calls Amazon Bedrock's multimodal
Converse API in `us-east-1` with the title's own declared band and a system
prompt asking one question: does this frame match that band. The model answers
`CLEAR` or `FLAGGED` with a plain-language reason; the service turns anything
else, including its own failure to reach Bedrock at all, into `UNSURE`.

**The model ID is a preference chain, not a constant.** `list-foundation-models`
and `list-inference-profiles` both list model IDs this account cannot actually
invoke, with nothing in either response distinguishing a real grant from a
catalogue entry — the only reliable check is a real Converse call.
`tools/bedrock_client.py`'s `MODEL_CHAIN` tries `us.anthropic.claude-sonnet-5`
first, falls back to `us.anthropic.claude-sonnet-4-6`, then
`us.anthropic.claude-sonnet-4-5-20250929-v1:0`, moving to the next entry on any
exception. Verified live: `sonnet-5` denies access in under a second, `sonnet-4-6`
answers in ~2.9 seconds. The build upgrades itself the day `sonnet-5` access
lands, with no code change.

**Why a service, not a direct call from the APK.** A Fire TV app that shipped
with an AWS credential embedded in it would be a real security defect, not a
demo shortcut. The Android client only ever sees an HTTP boundary
(`HttpFrameClassifierClient`, `10.0.2.2:8787` on the emulator), exactly the
shape a real deployment would use with its own backend. The client has a
connect and read timeout, and every failure — timeout, connection refused,
unparsable body — becomes `FrameJudgement.Unsure`, never a crash and never a
`Clear`. A `Settings` screen can test this connection live and reports
"Reachable" or "Unreachable" honestly.

**The escalation rule mirrors point 5.** A `Flagged` verdict moves a title's
effective band up by exactly one step (`AgeBand.escalate()`); nothing ever
moves it down. `Clear` and `Unsure` both leave the declared band untouched — an
`Unsure` verdict is not evidence of anything, so it changes nothing, the same
rule already applied to the remote signal. The gate itself
(`GateEngine.evaluateAttempt`) never sees the raw catalogue band directly; it
always receives whatever `ContentJudgementEngine.effectiveBand` computed, so
the safety property in points 2-5 holds identically whether the band came from
the catalogue or from an escalation.

**Where Bedrock does not belong.** The presence read (point 3) stays numeric on
purpose. Press cadence, hold duration and overshoot are thresholds a model adds
nothing to, a network call cannot meet the decision deadline in point 4, and a
model that could be talked into a pass on the presence question is the exact
failure this app was written to prevent. Vision only ever answers "does this
title's own frame match its own label", never "who is holding the remote".

**A narrow, disclosed race.** Classification is a real network round trip
(roughly 2-3 seconds against this account), kicked off the moment the app
launches. A select on a title before its judgement returns defers to the
catalogue's declared band, same as any title Bedrock never flagged — see
`FRICTION.md` for why this is documented rather than hidden.

## The correction, and the false-positive rate it produces

The published accuracy for a Made-for-Kids-style classification task, from the
one dataset in the research file, is 84.3%. Profile Gate does not repeat that
number as a claim about itself. Instead, the household log lets an adult mark
a `FLAGGED_REFUSED` event as wrong, behind a household PIN
(`ui/components/PinPad.kt`, a demo-fixed 4-digit PIN, disclosed as exactly
that: a deliberate-action gate, not a real account system). The coverage
screen then states, in the largest type on the page after the headline itself,
this household's own measured false-positive rate: corrections divided by
total flagged refusals, as a number, never the word "accurate". With zero
flagged events the screen says so and states no rate at all, rather than
implying a rate of zero means anything.

**A correction is record-only, permanently.** `GateSessionCorrections.kt`
exists as its own file for exactly one reason: `correctEvent` only ever sets a
`corrected` flag on a past `GateEvent`. It cannot replay, unblock, or change
what happened to the title that event was about. The asymmetry in point 5
("behaviour can only restrict, never unlock") holds for corrections too — the
worst a wrong flag costs is one recorded reversal on a rate a parent can read,
never a door that reopens.

## What it deliberately does not do

- **It does not inspect video.** Internal research into Fire TV / Fire OS platform
  facts and Fire TV family-profile behaviour establishes, from AOSP source, that a
  third-party Fire OS app cannot read frames
  or audio from another app's DRM-protected playback: `FLAG_SECURE`, protected
  decoder buffers, `CAPTURE_SECURE_VIDEO_OUTPUT` at `protectionLevel="signature"`,
  and no `AccessibilityService.takeScreenshot` below API 30 while Fire OS 16 is API
  28. Profile Gate never claims to see what is playing. It classifies who is
  reaching for it, and gates a title record it already holds, not a video frame it
  cannot reach.
- **It does not depend on `GetUserAgeData` answering.** It is called once, logged,
  and treated as a bonus. Every claim in this spec holds even if it always returns
  empty, which on this build it will.
- **It does not use a camera.** No Fire TV model was verified to expose one to a
  third-party app; the research file treats "no usable camera" as the planning
  assumption. Nothing here needs one.
- **It does not identify a specific person.** It never claims "this is your son." It
  classifies a remote-input pattern against a declared band, and says so.
- **It does not claim the behavioural read is validated.** No published study,
  dataset, or shipped product establishes that press cadence reliably separates a
  four-year-old from an adult. The spec does not hide this: see `FRICTION.md`,
  which keeps that account rather than rewriting it. The asymmetry
  in point 5 above is the whole defence — the worst-case failure of an unvalidated
  signal is a longer hold, never an exposed child.
- **A correction never unblocks anything.** See "The correction" above. It changes a
  measured rate on a screen. It never re-plays, re-opens, or clears a title.
- **Settings never edits the declared band.** The one control deliberately left out:
  a settings screen that could raise the band would be the one bypass this whole app
  exists to prevent. Settings is diagnostics only — see `SettingsScreen.kt`.

## Data

The catalogue (`CatalogRepository`) is ten invented titles with invented age bands,
obviously fictional ("Puddle Friends", "Nebula Cadets: Lockdown Special"), each
source-commented as mock data standing in for a real content partner's metadata. No
real title, real studio, or real Amazon catalogue data is used or implied anywhere in
the app or its tests.

Each title also carries one synthetic PNG "frame" under `app/src/main/assets/frames/`
(generated by `tools/gen_frames.py`-style logic at build time, not shipped as a real
video asset): flat-colour backgrounds with simple round, friendly shapes for the
in-band titles, and darker, angular shapes for the titles this catalogue mislabels or
already declares above-band. `t10`, "Sunny Meadow Friends", is declared `AGE_0_12`
like every other title in its row but carries the darker frame on purpose — the one
title built specifically to demonstrate the vision classifier catching a label its own
catalogue metadata got wrong, the exact Disney MFK failure this app exists for.

The household log persists across app restarts via `SharedPreferences`
(`EventLogStore`), encoded by `EventCodec`, a small delimited format chosen instead of
a JSON library specifically so the encode/decode round trip stays a plain, dependency-
free JVM unit test.

## Design

Checked against an internal survey of this batch's other apps' screens before picking anything. Nothing there uses a
near-black teal ground or a cool blue accent, so that is the direction: a harbour
light, not a parental-control dashboard. The screen reads like something that watches
quietly and rarely speaks, not a settings menu.

**Ground: deep-tide**, a near-black, faintly teal navy (`#0A1420`). Darker and cooler
than the dusk-purple, plum-black, navy and sidebar grounds already claimed by other
projects in the inventory, and unlike them it carries a slight aquatic cast on
purpose, because this product's whole metaphor is a light over water at night.

**Three accents, one per state, chosen so they read as three different shapes before
they read as three different colours** — the coordinator's note that a parent glances
across a room, not up close, is the reason this is a shape rule first and a colour
rule second:

- **Clear — signal-blue** (`#3B8EEA`), a slow horizontal sweep of light drifting
  continuously across the home screen's header, and nothing else: no card, no
  interruption. Motion reads as "still watching, nothing asked." Distinct in hue
  from every accent in the inventory (nothing there is blue) and in shape from the
  other two states because it is the only one that never stops moving and never
  produces a card at all.
- **Unsure — flare-tangerine** (`#FF8A3D`), a full-screen card with a ring that
  fills clockwise while a finger holds OK. The fill-arc is the shape cue: a ring
  visibly closing is legible at three metres even in monochrome, and it is the only
  one of the three states with a control on screen at all. Distinct from a coral
  (`#FF5B4F`) already claimed elsewhere in the wider survey, and from the amber
  tones several sibling apps in this batch use — a genuinely different hue family
  (orange, not amber-yellow or pink-red).
- **Flagged — ember-crimson** (`#C81E3A`), the same full-screen card shape as
  unsure but with no ring and no control at all, on purpose: nothing to press,
  nothing filling, nothing to do but read why and go back. Stillness is the point
  — clear moves continuously, unsure fills, flagged offers nothing to interact
  with — so the three states are readable from across a room by motion and layout
  alone, before anyone reads a word or a colour.

Log and coverage text sit in a pale **sea-glass** (`#DCEAEA`) on deep-tide; secondary
detail sits in a quieter **fog-grey** (`#8AA0A8`). Typography is a single rounded
geometric sans, sized for a ten-foot living-room viewing distance: body text 20-22sp,
headers 28-40sp, against the ISO 9241-303-derived floor of 20sp body / 24sp preferred
at three metres — with a glowing focus ring standing in for a spotlight rather than a
coloured border, since this is a D-pad interface with no cursor.

**The coverage report gets its own screen, not a footer.** `CoverageScreen` is a full
destination, reachable at the end of every session and from the home row at any time,
set in the largest type on the app after the gate card itself: exact counts of
attempts, decisions, and deadline misses, in the same sea-glass-on-deep-tide language
as the rest of the app, because "checked 94 per cent, three gaps, here they are" is
the most honest thing this product says and it should look like it, not read like a
settings sub-page.

**Focus and screen-reader notes, per the shared Fire TV craft reference.** Every interactive
element is a `Modifier.focusable()` target reached by real D-pad key events
(`onPreviewKeyEvent`/`onKeyEvent`), never a click handler alone — TitleTile, the
hold-to-confirm ring, every footer action, search results, PIN digits, the
"That was me" correction control. VoiceView rides Fire OS's own focus system, so a
focusable node with no readable text is invisible to it: `TitleTile` and the
hold-to-confirm ring each carry an explicit merged `contentDescription` (title,
band, and any vision-flag note as one sentence; the ring's live hold percentage)
rather than relying on fragments of child text. The three gate states are built to
read by motion and layout before colour (see above) precisely because Material 3's
default dark-surface elevation ramp is not real contrast at any distance, let alone
three metres, and this app's own ground/surface pair is no better by the numbers —
so the state distinction never depends on it.

## Licence

MIT. See `README.md`.
