#!/usr/bin/env python3
"""
Writes the ten catalogue frames from the photographs named in `frame_sources.py`,
and regenerates `ATTRIBUTION.md` from what Wikimedia Commons actually returns.

These are not decoration. Each one is the single frame Profile Gate sends to
Bedrock for that title, so the vision half of the product is a claim about
exactly these pixels — and a set of reference photographs of a real Fire TV
is blunt that on a real Fire TV "everything is artwork... nothing is a flat
rectangle with a word in it, and nothing is an emoji". A drawn illustration
is a third thing, better than a smiley face and still not what a television
is covered in. Putting our screens next to a real Fire TV named the gap as
photographic depth, so these are photographs.

The licence and author written into ATTRIBUTION.md come from the API response
on the run that produced the file, never from the table in `frame_sources.py`.

Run:
    tools/.venv/bin/python tools/build_frames.py
"""
from __future__ import annotations

import os
import sys
import time

from PIL import Image

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from fetch_frames import _get, _plain, download  # noqa: E402
from frame_sources import SOURCES  # noqa: E402

HERE = os.path.dirname(os.path.abspath(__file__))
OUT = os.path.join(HERE, "..", "app", "src", "main", "assets", "frames")
ATTRIBUTION = os.path.join(HERE, "..", "ATTRIBUTION.md")
W, H = 1024, 576


def patiently(call, *args):
    """
    Commons rate-limits, and answers a burst with 429 rather than with data.
    Several agents in this workspace have already spent an afternoon on that
    by retrying in a tight loop, so this waits longer each time and gives up
    loudly rather than hammering.
    """
    delay = 4
    for attempt in range(6):
        try:
            return call(*args)
        except Exception as exc:
            if "429" not in str(exc) or attempt == 5:
                raise
            print(f"  rate-limited, waiting {delay}s")
            time.sleep(delay)
            delay *= 2
    raise RuntimeError("unreachable")


def describe(file_title: str) -> dict:
    """Licence, author and file page, read from Commons on this run."""
    data = _get({
        "action": "query", "format": "json", "formatversion": "2",
        "titles": file_title, "prop": "imageinfo",
        "iiprop": "url|extmetadata|size", "iiurlwidth": "1800",
    })
    page = data["query"]["pages"][0]
    info = page["imageinfo"][0]
    meta = info.get("extmetadata", {})
    return {
        "title": page["title"],
        "thumb": info["thumburl"],
        "page": info["descriptionurl"],
        "licence": _plain(meta.get("LicenseShortName", {}).get("value", "")) or "unstated",
        "author": _plain(meta.get("Artist", {}).get("value", "")) or "unnamed",
    }


def crop_16_9(path: str, out_path: str) -> None:
    """Centre crop to 16:9 at 1024x576. No filter, no grade, no vignette."""
    with Image.open(path) as im:
        im = im.convert("RGB")
        width, height = im.size
        if width / height > W / H:
            new_width = int(height * W / H)
            box = ((width - new_width) // 2, 0, (width - new_width) // 2 + new_width, height)
        else:
            new_height = int(width * H / W)
            box = (0, (height - new_height) // 2, width, (height - new_height) // 2 + new_height)
        im.crop(box).resize((W, H), Image.LANCZOS).save(out_path, quality=92, optimize=True)


def main() -> None:
    os.makedirs(OUT, exist_ok=True)
    rows = []
    for name, (file_title, why) in sorted(SOURCES.items(), key=lambda kv: int(kv[0][1:])):
        source = patiently(describe, file_title)
        raw = os.path.join("/tmp", f"pg-frame-{name}.jpg")
        patiently(download, source["thumb"], raw)
        time.sleep(2)
        crop_16_9(raw, os.path.join(OUT, f"{name}.jpg"))
        os.remove(raw)
        rows.append((name, why, source))
        print(f"{name}: {source['licence']} — {source['title']}")

    with open(ATTRIBUTION, "w") as f:
        f.write(HEADER)
        for name, why, source in rows:
            f.write(
                f"### `{name}.png` — {why}\n\n"
                f"- **File**: [{source['title']}]({source['page']})\n"
                f"- **Author**: {source['author']}\n"
                f"- **Licence**: {source['licence']}\n\n"
            )
        f.write(FOOTER)
    print(f"wrote {os.path.normpath(ATTRIBUTION)}")


HEADER = """# Image attribution

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

"""

FOOTER = """## Everything else on screen

The four verdict marks — cleared, unsure, flagged and flagged-solid — are drawn
here, in `app/src/main/res/drawable/verdict_*.xml`, from the originals in
the hand-drawn source SVGs kept outside this app's own tree. They are deliberately not
photographs. A verdict is a symbol rather than a picture, it has to be legible
at 26 dp on a tile chip and at 116 dp on the gate card from the same drawing,
and it has to hold its meaning in a monochrome photograph taken from across a
room. A photograph does none of those things.
"""


if __name__ == "__main__":
    main()
