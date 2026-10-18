package com.kcalfit.app.data.remote

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class FoodHttpResponse(val status: Int, val body: String, val retryAfter: String? = null)
fun interface FoodHttpTransport { suspend fun get(url: String): FoodHttpResponse }

/** Blocking platform I/O is isolated; coroutine cancellation actively disconnects the socket. */
class UrlConnectionFoodTransport(
    private val userAgent: String,
    private val connectionFactory: (String) -> HttpURLConnection = { URL(it).openConnection() as HttpURLConnection }
) : FoodHttpTransport {
    override suspend fun get(url: String): FoodHttpResponse = withTimeoutOrNull(6_000) {
        suspendCancellableCoroutine { continuation ->
            val connection = connectionFactory(url).apply {
                requestMethod = "GET"
                connectTimeout = 3_000
                readTimeout = 5_000
                setRequestProperty("User-Agent", userAgent)
                setRequestProperty("Accept", "application/json")
            }
            val work = workers.submit {
                try {
                    val status = connection.responseCode
                    val body = if (status in 200..299) connection.inputStream.bufferedReader().use { reader ->
                        val text = StringBuilder()
                        val buffer = CharArray(8_192)
                        while (true) {
                            val count = reader.read(buffer)
                            if (count < 0) break
                            text.append(buffer, 0, count)
                            if (text.length > 2_000_000) throw IllegalStateException("Food response exceeded the supported size.")
                        }
                        text.toString()
                    } else ""
                    if (continuation.isActive) continuation.resume(FoodHttpResponse(status, body, connection.getHeaderField("Retry-After")))
                } catch (failure: Exception) {
                    if (continuation.isActive) continuation.resumeWithException(failure)
                } finally { connection.disconnect() }
            }
            continuation.invokeOnCancellation {
                work.cancel(true)
                // disconnect() can wait on an I/O lock; never run it on Compose's UI thread.
                closers.execute { connection.disconnect() }
            }
        }
    } ?: throw SocketTimeoutException("Food service request timed out.")

    companion object {
        private val workers = Executors.newFixedThreadPool(2) { task -> Thread(task, "food-http").apply { isDaemon = true } }
        private val closers = Executors.newFixedThreadPool(2) { task -> Thread(task, "food-http-close").apply { isDaemon = true } }
    }
}
