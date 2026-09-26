# Attribution

## The typeface

**IBM Plex Sans**, version 3.201, Copyright 2017 IBM Corp. with Reserved Font Name
"Plex", licensed under the **SIL Open Font License, Version 1.1**
(<http://scripts.sil.org/OFL>). The OFL permits bundling and redistribution inside
an application, including a commercial one, provided the font is not sold on its
own and the Reserved Font Name is not used for a modified version. Neither
happens here.

Four static weights ship in `app/src/main/res/font/`: regular (400), medium (500),
semibold (600) and bold (700). They were cut from the Google Fonts variable master
`ofl/ibmplexsans/IBMPlexSans[wdth,wght].ttf` at `wdth=100` with
`fontTools.varLib.instancer`. Instancing a variable font is not a modification in
the OFL's sense — the outlines are the ones IBM drew, at four of the weights the
master already defines — and the name records are the family's own, rewritten by
`updateFontNames` to the correct static style names.

It is here for the figures, and the claim was measured rather than read off a
specimen: in all four shipped files every digit from 0 to 9 advances exactly 600
of the 1000 units per em, so numbers align in a column without an OpenType feature
being asked for. An accountability record whose timestamps and counts do not line
up is not behaving like a record, which is the whole reason this app stopped
rendering in Roboto.

## Image attribution

Every photograph in this app comes from Wikimedia Commons under a licence that
permits redistribution and commercial use. The licence and author below were
read from the Commons API on the run that produced each file — see
`tools/build_frames.py`, which regenerates this page and the images together so
the two cannot drift apart.

Nothing here belongs to a streaming service. A subscription permits watching,
not republishing key art, and no Amazon, Netflix or other catalogue imagery
appears anywhere in this app.

The ten titles are invented. The photographs stand in for their key art, and
each was chosen so a multimodal model looking at it would reach the same
verdict a person would: see `tools/frame_sources.py`.

Each image is centre-cropped to 16:9 and resized to 1024x576. No other
alteration is made.

## Catalogue frames

### `t1.png` — Puddle Friends. Sun, green grass, a castle upside down in the water.

- **File**: [File:Malahide Castle Reflection in Rain Puddle.JPG](https://commons.wikimedia.org/wiki/File:Malahide_Castle_Reflection_in_Rain_Puddle.JPG)
- **Author**: Gallftree008
- **Licence**: CC BY-SA 4.0

### `t2.jpg` — Story Barn. A red barn on green pasture, a horse grazing, under a fair-weather sky.

- **File**: [File:Hopewell MD3.jpg](https://commons.wikimedia.org/wiki/File:Hopewell_MD3.jpg)
- **Author**: Acroterion
- **Licence**: CC BY-SA 4.0

Replaced 2026-09-25: the previous pick (Union Town Red Barn) carried a visible
photographer's watermark baked into the frame, which is disqualifying for
something standing in as a show's own key art — see
an internal progress note's item 4. This file's extension is
corrected to `.jpg` to match what actually ships in
`app/src/main/assets/frames/`; the other nine entries below still say `.png`,
which was already stale before this pass and is left as found.

### `t3.png` — Nebula Cadets. Hubble's Westerlund 2 field: deep sky with no threat in it.

- **File**: [File:NASA Unveils Celestial Fireworks as Official Hubble 25th Anniversary Image.jpg](https://commons.wikimedia.org/wiki/File:NASA_Unveils_Celestial_Fireworks_as_Official_Hubble_25th_Anniversary_Image.jpg)
- **Author**: NASA, ESA, the Hubble Heritage Team (STScI/AURA), A. Nota (ESA/STScI), and the Westerlund 2 Science Team
- **Licence**: Public domain

### `t4.png` — Counting Tugboats. A red tug at a wharf in flat daylight.

- **File**: [File:Tugboat Svitzer Falcon moored in Fremantle Harbour, July 2021 02.jpg](https://commons.wikimedia.org/wiki/File:Tugboat_Svitzer_Falcon_moored_in_Fremantle_Harbour,_July_2021_02.jpg)
- **Author**: Calistemon
- **Licence**: CC BY-SA 4.0

### `t5.png` — Backyard Builders. A raised wooden playhouse in a sunlit summer garden.

- **File**: [File:Kelvedon Hall raised wooden playhouse Kelvedon Hatch Essex England 01.jpg](https://commons.wikimedia.org/wiki/File:Kelvedon_Hall_raised_wooden_playhouse_Kelvedon_Hatch_Essex_England_01.jpg)
- **Author**: Acabashi
- **Licence**: CC BY-SA 4.0

### `t6.png` — Nebula Cadets: Lockdown Special. A black corridor and one lit doorway.

- **File**: [File:Exit - Flickr - frank hb.jpg](https://commons.wikimedia.org/wiki/File:Exit_-_Flickr_-_frank_hb.jpg)
- **Author**: frank_hb from Hamburg
- **Licence**: CC BY 2.0

### `t7.png` — Midnight Garage. Wet asphalt, parked cars, sodium light, nobody about.

- **File**: [File:LISBON IN THE NIGHT RAIN (10604014063).jpg](https://commons.wikimedia.org/wiki/File:LISBON_IN_THE_NIGHT_RAIN_(10604014063).jpg)
- **Author**: Terry Kearney from liverpool, merseyside
- **Licence**: CC0

### `t8.png` — Late Edition. Lit office windows at night, cold, no faces.

- **File**: [File:Urban Luminescence (54084218924).jpg](https://commons.wikimedia.org/wiki/File:Urban_Luminescence_(54084218924).jpg)
- **Author**: Tugrul Ates from Amsterdam, Netherlands
- **Licence**: CC BY 2.0

### `t9.png` — Puddle Friends: Bedtime. A quiet flower meadow under mountains.

- **File**: [File:Wildflower Meadow (6997737191).jpg](https://commons.wikimedia.org/wiki/File:Wildflower_Meadow_(6997737191).jpg)
- **Author**: Mount Rainier National Park from Ashford, WA, United States
- **Licence**: Public domain

### `t10.png` — Sunny Meadow Friends. Lightning over a city at night. Its own label says 0-12 and this is what its own frame shows — the mislabel the classifier exists to catch, and the reason it can be caught at all.

- **File**: [File:Lightning (215415019).jpeg](https://commons.wikimedia.org/wiki/File:Lightning_(215415019).jpeg)
- **Author**: Burak Demir
- **Licence**: CC BY 3.0

## Everything else on screen

The four verdict marks — cleared, unsure, flagged and flagged-solid — are drawn
here, in `app/src/main/res/drawable/verdict_*.xml`, from the originals in
the hand-drawn source SVGs kept outside this app's own tree. They are deliberately not
photographs. A verdict is a symbol rather than a picture, it has to be legible
at 26 dp on a tile chip and at 116 dp on the gate card from the same drawing,
and it has to hold its meaning in a monochrome photograph taken from across a
room. A photograph does none of those things.
