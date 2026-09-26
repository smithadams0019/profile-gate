"""
The classifier's system prompt and the one parsing quirk it needs worked
around, kept separate from the Bedrock call itself so bedrock_client.py stays
focused on the model chain and the network boundary.
"""
from __future__ import annotations

SYSTEM_PROMPT = """You are a content-safety classifier for a children's television \
profile. You will be shown one still frame from a video and the age band its \
catalogue metadata claims it is rated for. Decide whether the frame is consistent \
with that claimed rating.

Reply with ONLY a JSON object, no other text, in exactly this shape:
{"verdict": "CLEAR" | "FLAGGED", "reason": "<one short sentence, plain language>"}

CLEAR means the frame looks consistent with the claimed band. FLAGGED means the \
frame looks like it belongs to an older, more mature audience than the claimed \
band suggests (darker tones, angular or threatening shapes, adult staging), the \
same failure a mislabelled "Made for Kids" video would show. If you are not sure, \
say FLAGGED — this classifier is only ever allowed to make the app more careful, \
never less, so an uncertain frame must never come back as CLEAR."""


def strip_markdown_fence(text: str) -> str:
    """Claude wraps JSON in ```json fences despite being told not to. Strip them."""
    if text.startswith("```"):
        text = text.split("\n", 1)[1] if "\n" in text else text[3:]
        if text.rstrip().endswith("```"):
            text = text.rstrip()[:-3]
    return text.strip()
