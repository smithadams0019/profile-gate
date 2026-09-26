#!/usr/bin/env python3
"""
Fetches the ten catalogue frames as real photographs from Wikimedia Commons.

Why Commons rather than a stock site: this submission goes public, so every
image in it has to be one we can show a licence for. Commons returns the
licence, the author and the file page as machine-readable metadata on the same
request that returns the image, so `ATTRIBUTION.md` is generated from what the
server actually said rather than from a claim on a landing page. Anything whose
licence forbids commercial use or derivative works is rejected here, not
checked by hand later.

Nothing from a streaming service is used anywhere. A Netflix subscription
permits watching, not republishing their key art.

Usage:
    tools/.venv/bin/python tools/fetch_frames.py --candidates   # contact sheet
    tools/.venv/bin/python tools/fetch_frames.py --build        # write the frames
"""
from __future__ import annotations

import argparse
import json
import os
import urllib.parse
import urllib.request

API = "https://commons.wikimedia.org/w/api.php"
UA = "ProfileGate-frame-fetch/1.0 (hackathon demo; contact via repo)"

# Licences that permit redistribution and commercial use. Anything with NC or
# ND in it is not on this list and is dropped.
ALLOWED = (
    "cc0", "public domain", "pd-", "cc by 4.0", "cc by 3.0", "cc by 2.0",
    "cc by-sa 4.0", "cc by-sa 3.0", "cc by-sa 2.0", "cc by-sa 1.0",
)


def _get(params: dict) -> dict:
    url = f"{API}?{urllib.parse.urlencode(params)}"
    request = urllib.request.Request(url, headers={"User-Agent": UA})
    with urllib.request.urlopen(request, timeout=30) as response:
        return json.load(response)


def search(query: str, limit: int = 24) -> list[dict]:
    """Landscape photographs matching `query` whose licence allows redistribution."""
    data = _get({
        "action": "query", "format": "json", "formatversion": "2",
        "generator": "search", "gsrsearch": f"filetype:bitmap {query}",
        "gsrnamespace": "6", "gsrlimit": str(limit),
        "prop": "imageinfo", "iiprop": "url|extmetadata|size", "iiurlwidth": "1600",
    })
    return [r for r in (_keep(p) for p in data.get("query", {}).get("pages", [])) if r]


def search_category(category: str, limit: int = 60) -> list[dict]:
    """The same licence-checked landscape photographs, but read off a Commons
    category rather than the full-text index.

    Full-text search on Commons ranks by filename and description text, which
    for a query like "living room" returns paintings of dining rooms and museum
    catalogue scans. The categories are curated by hand and are where the
    photographs actually live, so a category listing gives usable material
    where the same words typed as a query give almost none. Both paths run
    through `_keep`, so there is one licence check rather than two.
    """
    data = _get({
        "action": "query", "format": "json", "formatversion": "2",
        "generator": "categorymembers", "gcmtitle": category,
        "gcmtype": "file", "gcmlimit": str(limit),
        "prop": "imageinfo", "iiprop": "url|extmetadata|size", "iiurlwidth": "1600",
    })
    return [r for r in (_keep(p) for p in data.get("query", {}).get("pages", [])) if r]


def _keep(page: dict) -> dict | None:
    """Licence and shape gate. The one place either search path can say yes."""
    info = (page.get("imageinfo") or [{}])[0]
    meta = info.get("extmetadata", {})
    licence = (meta.get("LicenseShortName", {}).get("value") or "").strip()
    width, height = info.get("width", 0), info.get("height", 1)
    if not licence or not any(licence.lower().startswith(ok) for ok in ALLOWED):
        return None
    if "nc" in licence.lower().split() or "-nd" in licence.lower():
        return None
    if width < 1100 or width / max(height, 1) < 1.25:
        return None
    return {
        "title": page["title"],
        "thumb": info.get("thumburl"),
        "page": info.get("descriptionurl"),
        "licence": licence,
        "author": _plain(meta.get("Artist", {}).get("value", "")) or "unnamed",
        "credit": _plain(meta.get("Credit", {}).get("value", "")),
    }


def _plain(html: str) -> str:
    """Commons returns author and credit as HTML fragments."""
    text, depth = [], 0
    for ch in html:
        if ch == "<":
            depth += 1
        elif ch == ">":
            depth -= 1
        elif depth == 0:
            text.append(ch)
    return " ".join("".join(text).split())[:160]


def download(url: str, path: str) -> None:
    request = urllib.request.Request(url, headers={"User-Agent": UA})
    with urllib.request.urlopen(request, timeout=60) as response, open(path, "wb") as f:
        f.write(response.read())


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--query")
    parser.add_argument("--category", help='e.g. "Category:Television sets"')
    parser.add_argument("--out", required=True, help="directory for candidate thumbnails")
    parser.add_argument("--limit", type=int, default=24)
    args = parser.parse_args()

    os.makedirs(args.out, exist_ok=True)
    if not args.query and not args.category:
        parser.error("one of --query or --category is required")
    results = (search_category(args.category, max(args.limit, 60)) if args.category
               else search(args.query, args.limit))
    manifest = []
    for i, r in enumerate(results[:12]):
        path = os.path.join(args.out, f"{i:02d}.jpg")
        try:
            download(r["thumb"], path)
        except Exception as exc:  # a Commons thumb can 404 for exotic formats
            print(f"skip {r['title']}: {exc}")
            continue
        r["file"] = path
        manifest.append(r)
    with open(os.path.join(args.out, "manifest.json"), "w") as f:
        json.dump(manifest, f, indent=2)
    for i, r in enumerate(manifest):
        print(f"{i:02d}  {r['licence']:<16}  {r['title']}")


if __name__ == "__main__":
    main()
