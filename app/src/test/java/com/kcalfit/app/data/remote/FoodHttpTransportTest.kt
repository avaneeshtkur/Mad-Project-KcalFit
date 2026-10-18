package com.kcalfit.app.data.remote

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class FoodHttpTransportTest {
    @Test fun cancellingARequestDisconnectsOffTheCallingThreadWithoutWaitingForTheSocketLock() = runBlocking {
        val started = CountDownLatch(1)
        val closing = CountDownLatch(1)
        val releaseClose = CountDownLatch(1)
        val closeExited = AtomicBoolean(false)
        val onCallingThread = AtomicBoolean(false)
        val caller = Thread.currentThread()
        val connection = object : HttpURLConnection(URL("https://example.invalid")) {
            override fun connect() { }
            override fun usingProxy() = false
            override fun getResponseCode(): Int {
                started.countDown()
                try { CountDownLatch(1).await(3, TimeUnit.SECONDS) }
                catch (_: InterruptedException) { throw IOException("cancelled") }
                return 200
            }
            override fun disconnect() {
                if (Thread.currentThread() == caller) onCallingThread.set(true)
                closing.countDown()
                releaseClose.await(3, TimeUnit.SECONDS)
                closeExited.set(true)
            }
        }
        val transport = UrlConnectionFoodTransport("Calfit-test") { connection }
        val request = launch(start = CoroutineStart.UNDISPATCHED) { transport.get("https://example.invalid") }
        try {
            assertTrue(started.await(1, TimeUnit.SECONDS))
            request.cancelAndJoin()
            assertTrue(closing.await(1, TimeUnit.SECONDS))
            assertFalse(onCallingThread.get())
            assertFalse(closeExited.get())
        } finally { releaseClose.countDown(); request.cancelAndJoin() }
    }
}
