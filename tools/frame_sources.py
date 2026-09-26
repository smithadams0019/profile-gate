#!/usr/bin/env python3
"""
Which photograph stands in for which mock title, and why that one.

Six of these have to look like something a 0-12 profile may watch and four
have to look like something it may not, because the vision half of this
product is a real Bedrock call against exactly these pixels. `t10` is the
whole argument: its catalogue entry claims 0-12 and calls itself a sunny
meadow, and the frame is a lightning storm at night. If that photograph were
merely neutral, the mislabel the classifier catches would be theatre.

Every file is on Wikimedia Commons under a licence that permits
redistribution and commercial use, and `build_frames.py` re-reads the licence
and author from the API rather than trusting this table. Nothing from a
streaming service is used anywhere: a subscription permits watching, not
republishing key art.
"""

SOURCES = {
    "t1": ("File:Malahide Castle Reflection in Rain Puddle.JPG",
           "Puddle Friends. Sun, green grass, a castle upside down in the water."),
    # Was a barn silhouetted in gold fog at dusk. Bedrock flagged it, and was
    # right to: "moody, heavily atmospheric... more like adult documentary".
    # The photograph was wrong for the title, not the model.
    #
    # The next pick (Union Town Red Barn) fixed that read but carried a
    # visible photographer's watermark baked into the frame -- "Knowlesgalle"
    # in the bottom right corner, sized for a browser thumbnail rather than a
    # television. an internal design review's item 4 names this set as
    # not yet believable as real key art, and a watermark is the single most
    # concrete way a frame announces itself as a downloaded photograph rather
    # than artwork commissioned for a show. Swapped 2026-09-25 for a clean
    # frame in the same cheerful-daylight register the first fix established
    # (no re-run against Bedrock in this pass -- see ATTRIBUTION.md).
    "t2": ("File:Hopewell MD3.jpg",
           "Story Barn. A red barn on green pasture, a horse grazing, under a fair-weather sky."),
    "t3": ("File:NASA Unveils Celestial Fireworks as Official Hubble 25th Anniversary Image.jpg",
           "Nebula Cadets. Hubble's Westerlund 2 field: deep sky with no threat in it."),
    "t4": ("File:Tugboat Svitzer Falcon moored in Fremantle Harbour, July 2021 02.jpg",
           "Counting Tugboats. A red tug at a wharf in flat daylight."),
    # Was a treehouse platform in a bare winter wood, which Bedrock read as
    # "bare, shadowy forest... no children's programming indicators". Same
    # lesson as t2: pick a frame that means what its title says.
    "t5": ("File:Kelvedon Hall raised wooden playhouse Kelvedon Hatch Essex England 01.jpg",
           "Backyard Builders. A raised wooden playhouse in a sunlit summer garden."),
    "t6": ("File:Exit - Flickr - frank hb.jpg",
           "Nebula Cadets: Lockdown Special. A black corridor and one lit doorway."),
    "t7": ("File:LISBON IN THE NIGHT RAIN (10604014063).jpg",
           "Midnight Garage. Wet asphalt, parked cars, sodium light, nobody about."),
    "t8": ("File:Urban Luminescence (54084218924).jpg",
           "Late Edition. Lit office windows at night, cold, no faces."),
    # Was a moonlit pier. Bedrock flagged it as "adult documentary or travel
    # content" and that was a fair reading of the picture — a title called
    # Bedtime does not have to be photographed at night, and a frame this app
    # would have to argue with is the wrong frame.
    "t9": ("File:Wildflower Meadow (6997737191).jpg",
           "Puddle Friends: Bedtime. A quiet flower meadow under mountains."),
    "t10": ("File:Lightning (215415019).jpeg",
            "Sunny Meadow Friends. Lightning over a city at night. Its own label "
            "says 0-12 and this is what its own frame shows — the mislabel the "
            "classifier exists to catch, and the reason it can be caught at all."),
}
