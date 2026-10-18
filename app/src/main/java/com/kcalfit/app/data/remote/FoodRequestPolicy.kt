package com.kcalfit.app.data.remote

import com.kcalfit.app.data.search.FoodSearchDiagnostics
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import java.io.IOException
import java.net.SocketTimeoutException
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.max
import kotlin.random.Random

class FoodServiceException(val status: Int, val retryAfterMillis: Long? = null) : IOException(when (status) {
    429 -> "Food search is busy. Please wait before retrying."
    500, 502, 503, 504 -> "Open Food Facts is temporarily unavailable ($status). Please try again shortly."
    401, 403 -> "Food service access was refused ($status). Retrying immediately will not help."
    else -> "Food service returned $status."
})

/** The only automatic retry layer. The shared gate includes retries and manual attempts. */
class FoodRequestPolicy(
    private val minIntervalMillis: Long = 6_500,
    private val maxRetries: Int = 2,
    private val overallTimeoutMillis: Long = 24_000,
    private val clock: () -> Long = System::currentTimeMillis,
    private val sleep: suspend (Long) -> Unit = { delay(it) },
    private val jitter: () -> Long = { Random.nextLong(0, 251) },
    private val diagnostics: FoodSearchDiagnostics = FoodSearchDiagnostics.NONE
) {
    private val gate = Mutex()
    private var nextAllowedAt = 0L

    suspend fun <T : Any> execute(block: suspend () -> T): T = withTimeoutOrNull(overallTimeoutMillis) {
        gate.withLock {
            var retry = 0
            while (true) {
                val wait = (nextAllowedAt - clock()).coerceAtLeast(0)
                if (wait > 0) sleep(wait)
                nextAllowedAt = clock() + minIntervalMillis
                val started = clock()
                try {
                    val value = block()
                    diagnostics.record("http", clock() - started, 0, 200, retry)
                    return@withLock value
                } catch (cancelled: CancellationException) {
                    diagnostics.record("http_cancelled", clock() - started, 0, 0, retry)
                    throw cancelled
                } catch (failure: IOException) {
                    val status = (failure as? FoodServiceException)?.status ?: 0
                    diagnostics.record("http", clock() - started, 0, status, retry)
                    val serverDelay = (failure as? FoodServiceException)?.retryAfterMillis ?: 0
                    val deadline = runCatching { Math.addExact(clock(), serverDelay) }.getOrDefault(Long.MAX_VALUE)
                    nextAllowedAt = max(nextAllowedAt, deadline)
                    val transient = failure !is FoodServiceException || status in setOf(429, 500, 502, 503, 504)
                    if (!transient || retry >= maxRetries) throw failure
                    val backoff = 1_000L * (1L shl retry) + jitter()
                    nextAllowedAt = max(nextAllowedAt, clock() + backoff)
                    retry++
                }
            }
            @Suppress("UNREACHABLE_CODE") error("Unreachable")
        }
    } ?: throw SocketTimeoutException("Food search timed out. Please retry when the connection or service recovers.")

    companion object {
        fun retryAfterMillis(value: String?, now: Long): Long? {
            if (value.isNullOrBlank()) return null
            value.trim().toLongOrNull()?.let { return it.coerceIn(0, Long.MAX_VALUE / 1_000) * 1_000 }
            return runCatching {
                (ZonedDateTime.parse(value.trim(), DateTimeFormatter.RFC_1123_DATE_TIME).toInstant().toEpochMilli() - now)
                    .coerceAtLeast(0)
            }.getOrNull()
        }
    }
}
