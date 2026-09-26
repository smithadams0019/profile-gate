"""
The Bedrock call itself, isolated from the HTTP server around it. See
frame_classifier_service.py for why this runs as a separate local service
instead of inside the Android app.
"""
from __future__ import annotations

import json
import logging
import time

import boto3
from botocore.config import Config

from classifier_prompt import SYSTEM_PROMPT, strip_markdown_fence

log = logging.getLogger("frame-classifier")

REGION = "us-east-1"

# Newest first, oldest-known-working last. `list-foundation-models` and
# `list-inference-profiles` both list models this account cannot actually
# invoke -- nothing in either response distinguishes a real grant from a
# catalogue entry, so the only reliable check is a real Converse call
# (verified 2026-09-22, see FRICTION.md). Rather than hard-code
# the one ID confirmed today, this tries each in order and falls back down
# the list on any failure, so the build upgrades itself the day access to a
# newer model lands, with no code change.
MODEL_CHAIN = [
    "us.anthropic.claude-sonnet-5",
    "us.anthropic.claude-sonnet-4-6",
    "us.anthropic.claude-sonnet-4-5-20250929-v1:0",
]

_client = boto3.client(
    "bedrock-runtime",
    region_name=REGION,
    config=Config(connect_timeout=3, read_timeout=6, retries={"max_attempts": 0}),
)


def _image_format(image_bytes: bytes) -> str:
    """
    Read the format off the bytes rather than naming it.

    Bedrock rejects the call outright when the declared media type and the
    actual one disagree, and a hard-coded "png" survived the catalogue frames
    being replaced with photographs: every classification came back UNSURE with
    a validation error, and the app correctly reported "0/10 checked" while
    looking, from the outside, simply broken.
    """
    return "png" if image_bytes[:8] == b"\x89PNG\r\n\x1a\n" else "jpeg"


def _converse_once(model_id: str, image_bytes: bytes, title: str, declared_band_label: str) -> dict:
    """One attempt against one model. Raises on any failure; never returns UNSURE itself."""
    response = _client.converse(
        modelId=model_id,
        system=[{"text": SYSTEM_PROMPT}],
        messages=[
            {
                "role": "user",
                "content": [
                    {"image": {"format": _image_format(image_bytes), "source": {"bytes": image_bytes}}},
                    {
                        "text": f'Title: "{title}". Claimed band: {declared_band_label}. '
                        "Judge the frame above."
                    },
                ],
            }
        ],
        inferenceConfig={"maxTokens": 200, "temperature": 0},
    )
    text = response["output"]["message"]["content"][0]["text"].strip()
    parsed = json.loads(strip_markdown_fence(text))
    verdict = parsed.get("verdict")
    reason = str(parsed.get("reason", ""))[:300]
    if verdict not in ("CLEAR", "FLAGGED"):
        raise ValueError(f"unexpected verdict field: {verdict!r}")
    return {"verdict": verdict, "reason": reason}


def classify_frame(image_bytes: bytes, title: str, declared_band_label: str) -> dict:
    """Always returns a result dict. Walks MODEL_CHAIN newest-first; every model
    failing, or the call itself failing, becomes a safe UNSURE -- never a crash
    and never a CLEAR the network could not actually justify."""
    started = time.monotonic()
    last_exc: Exception | None = None
    for model_id in MODEL_CHAIN:
        try:
            result = _converse_once(model_id, image_bytes, title, declared_band_label)
            elapsed_ms = int((time.monotonic() - started) * 1000)
            log.info("classified %s -> %s via %s (%dms)", title, result["verdict"], model_id, elapsed_ms)
            return {**result, "source": "bedrock", "latencyMs": elapsed_ms}
        except Exception as exc:  # noqa: BLE001 - try the next model in the chain
            log.warning("model %s failed for %s: %s", model_id, title, exc)
            last_exc = exc
    elapsed_ms = int((time.monotonic() - started) * 1000)
    log.warning("all models in chain failed for %s after %dms: %s", title, elapsed_ms, last_exc)
    return {
        "verdict": "UNSURE",
        "reason": f"Bedrock unreachable or every model in the chain failed: {last_exc}",
        "source": "fallback",
        "latencyMs": elapsed_ms,
    }
