package com.gdstudio.music.next.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

data class FetchedFile(
    val bytes: ByteArray,
    val mimeType: String,
)

/**
 * Plain HTTP file transfer used by downloads.
 *
 * Kept separate from [GdMusicApi] because it talks to the music source hosts directly instead
 * of the signed `api.php` endpoint, and because it streams into a file rather than parsing JSON.
 */
class HttpFileClient(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .callTimeout(0, TimeUnit.MILLISECONDS)
        .build(),
) {
    /** Fetches a small resource such as cover art into memory. */
    suspend fun fetch(url: String, maxBytes: Long = MAX_INLINE_BYTES): FetchedFile =
        withContext(Dispatchers.IO) {
            client.newCall(request(url)).execute().use { response ->
                if (!response.isSuccessful) throw IOException("资源返回 ${response.code}")
                val body = response.body
                val declaredLength = body.contentLength()
                if (declaredLength > maxBytes) throw IOException("资源过大，已跳过")
                val buffer = ByteArrayOutputStreamWithLimit(maxBytes)
                body.byteStream().use { input ->
                    val chunk = ByteArray(DEFAULT_BUFFER_SIZE)
                    while (true) {
                        currentCoroutineContext().ensureActive()
                        val read = input.read(chunk)
                        if (read < 0) break
                        buffer.write(chunk, 0, read)
                    }
                }
                FetchedFile(
                    bytes = buffer.toByteArray(),
                    mimeType = body.contentType()?.toString()?.substringBefore(';') ?: "image/jpeg",
                )
            }
        }

    /** Streams a remote file to disk, reporting `(receivedBytes, totalBytes)` while copying. */
    suspend fun download(
        url: String,
        target: File,
        onProgress: (receivedBytes: Long, totalBytes: Long) -> Unit,
    ): Long = withContext(Dispatchers.IO) {
        var total = 0L
        client.newCall(request(url)).execute().use { response ->
            if (!response.isSuccessful) throw IOException("音乐源返回 ${response.code}")
            val body = response.body
            val declaredLength = body.contentLength()
            target.outputStream().use { output ->
                body.byteStream().use { input ->
                    val chunk = ByteArray(DEFAULT_BUFFER_SIZE)
                    var received = 0L
                    var lastReport = 0L
                    while (true) {
                        currentCoroutineContext().ensureActive()
                        val read = input.read(chunk)
                        if (read < 0) break
                        output.write(chunk, 0, read)
                        received += read
                        if (received - lastReport >= PROGRESS_STEP_BYTES) {
                            lastReport = received
                            onProgress(received, declaredLength)
                        }
                    }
                    total = received
                }
            }
            onProgress(total, declaredLength)
        }
        total
    }

    private fun request(url: String): Request = Request.Builder()
        .url(url)
        .header("User-Agent", USER_AGENT)
        .header("Referer", REFERRER)
        .get()
        .build()

    /** Throws instead of buffering past the limit so a hostile host cannot exhaust memory. */
    private class ByteArrayOutputStreamWithLimit(private val limit: Long) : java.io.ByteArrayOutputStream() {
        override fun write(bytes: ByteArray, offset: Int, length: Int) {
            if (size() + length > limit) throw IOException("资源过大，已跳过")
            super.write(bytes, offset, length)
        }

        override fun write(oneByte: Int) {
            if (size() + 1 > limit) throw IOException("资源过大，已跳过")
            super.write(oneByte)
        }
    }

    private companion object {
        const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/126 Safari/537.36"
        const val REFERRER = "https://music.gdstudio.xyz/"
        const val MAX_INLINE_BYTES = 12L * 1024 * 1024
        const val PROGRESS_STEP_BYTES = 64L * 1024
    }
}
