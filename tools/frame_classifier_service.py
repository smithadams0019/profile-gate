#!/usr/bin/env python3
"""
Frame classifier service — the "program that decides whether a video should be
labelled for kids" that the Disney order made a studio build by hand. Profile
Gate's Android client sends the frame it decoded itself (see SPEC.md, "What it
deliberately does not do": it can never decode another app's video, so this only
ever runs against the mock catalogue's own PNG frames in
app/src/main/assets/frames/). The actual Bedrock call lives in bedrock_client.py;
this file is only the HTTP boundary around it.

Why a separate local service instead of calling Bedrock straight from the
Android app: a Fire TV app that shipped with an AWS secret key embedded in its
APK would be a real security bug, not a hackathon shortcut. A thin backend that
holds the credentials, and an Android client that only ever sees an HTTP
boundary, is the architecture a real deployment would use, so that is what this
demo uses too. The client treats this service exactly like it treats
GetUserAgeData: call it, log what came back, and if it does not answer in time,
treat that as UNSURE and never let it clear anything.

Run:
    tools/.venv/bin/python tools/frame_classifier_service.py

Listens on 127.0.0.1:8787. The Android emulator reaches the host machine at
10.0.2.2, so the client's base URL is http://10.0.2.2:8787.
"""
from __future__ import annotations

import base64
import json
import logging
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer

from bedrock_client import classify_frame

logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(message)s")
log = logging.getLogger("frame-classifier")

FALLBACK_LATENCY_MS = 0


def _fallback(reason: str) -> dict:
    return {"verdict": "UNSURE", "reason": reason, "source": "fallback", "latencyMs": FALLBACK_LATENCY_MS}


class Handler(BaseHTTPRequestHandler):
    def log_message(self, fmt, *args):  # quiet the default stderr access log
        log.debug(fmt, *args)

    def _write_json(self, payload: dict) -> None:
        body = json.dumps(payload).encode()
        self.send_response(200)
        self.send_header("Content-Type", "application/json")
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)

    def do_POST(self):
        if self.path != "/classify":
            self.send_response(404)
            self.end_headers()
            return
        length = int(self.headers.get("Content-Length", 0))
        try:
            body = json.loads(self.rfile.read(length))
            image_bytes = base64.b64decode(body["imageBase64"])
            result = classify_frame(image_bytes, body["title"], body["declaredBandLabel"])
            self._write_json(result)
        except Exception as exc:  # noqa: BLE001 - a malformed request also fails closed
            log.warning("bad request: %s", exc)
            self._write_json(_fallback(f"bad request: {exc}"))

    def do_GET(self):
        if self.path == "/health":
            self.send_response(200)
            self.end_headers()
            self.wfile.write(b"ok")
        else:
            self.send_response(404)
            self.end_headers()


if __name__ == "__main__":
    server = ThreadingHTTPServer(("127.0.0.1", 8787), Handler)
    log.info("frame classifier service listening on http://127.0.0.1:8787")
    log.info("Android emulator reaches this at http://10.0.2.2:8787")
    server.serve_forever()
