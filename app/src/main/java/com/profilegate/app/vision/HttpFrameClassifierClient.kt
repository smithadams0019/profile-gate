package com.profilegate.app.vision

import android.content.res.AssetManager
import android.util.Base64
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/**
 * Sends the frame this mock title carries (bundled at build time under
 * `assets/frames/<titleId>.jpg`, never a real video frame, see SPEC.md) to the
 * local frame classifier service, which calls Amazon Bedrock's multimodal
 * Converse API for real. See tools/frame_classifier_service.py for why that
 * call happens behind an HTTP boundary rather than inside this app: no AWS
 * credential ever ships inside the APK.
 *
 * Every failure — connect timeout, read timeout, connection refused because
 * the local service is not running, a malformed response — becomes
 * [FrameJudgement.Unsure]. This never throws into the UI. See this build's own engineering brief's
 * rule that every model call needs a timeout, a fallback, and a stubbed test.
 */
class HttpFrameClassifierClient(
    private val assets: AssetManager,
    private val baseUrl: String = DEFAULT_BASE_URL,
    private val connectTimeoutMillis: Int = 4_000,
    // bedrock_client.py's own MODEL_CHAIN tries up to 3 models sequentially,
    // each with a 3s connect / 6s read budget at the boto3 level, and that
    // read timeout is inactivity-based rather than a hard cap on total call
    // duration -- a single successful call can legitimately run well past
    // 6s wall clock. Real classifications logged by tools/frame_classifier_
    // service.py routinely take 9-24s end to end (measured live 2026-09-26,
    // see video/TIMING-PROFILEGATE-VISION.md), so the previous 7_000ms read
    // timeout here gave up before the server's own real, correct answer
    // existed. classifyAll() calls this once per title with no retry, so a
    // premature timeout here was not recoverable later in the session --
    // the judgement stayed Unsure forever, even though Bedrock had already
    // answered. 35s comfortably covers every real latency observed.
    private val readTimeoutMillis: Int = 35_000,
) : FrameClassifierClient {

    override fun classify(titleId: String, titleName: String, declaredBandLabel: String): FrameJudgement {
        return try {
            val imageBytes = assets.open("frames/$titleId.jpg").use { it.readBytes() }
            val imageBase64 = Base64.encodeToString(imageBytes, Base64.NO_WRAP)
            val body = requestBody(imageBase64, titleName, declaredBandLabel)
            val responseText = post(body)
            VerdictParser.parse(responseText)
        } catch (e: IOException) {
            FrameJudgement.Unsure("frame classifier service unreachable: ${e.message}")
        } catch (e: Exception) {
            FrameJudgement.Unsure("unexpected error calling frame classifier: ${e.javaClass.simpleName}")
        }
    }

    private fun post(body: String): String {
        val connection = URL("$baseUrl/classify").openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.doOutput = true
        connection.connectTimeout = connectTimeoutMillis
        connection.readTimeout = readTimeoutMillis
        connection.setRequestProperty("Content-Type", "application/json")
        connection.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
        return try {
            connection.inputStream.use { it.readBytes().toString(Charsets.UTF_8) }
        } finally {
            connection.disconnect()
        }
    }

    private fun requestBody(imageBase64: String, title: String, declaredBandLabel: String): String {
        fun escape(s: String) = s.replace("\\", "\\\\").replace("\"", "\\\"")
        return """{"imageBase64":"$imageBase64","title":"${escape(title)}","declaredBandLabel":"${escape(declaredBandLabel)}"}"""
    }

    companion object {
        /** 10.0.2.2 is the Android emulator's alias for the host machine's loopback. */
        const val DEFAULT_BASE_URL = "http://10.0.2.2:8787"
    }
}
