package co.ke.xently.shopping.libraries.data.source.utils

import co.ke.xently.shopping.libraries.data.source.R
import kotlinx.coroutines.delay
import java.net.ConnectException
import java.net.SocketTimeoutException
import kotlin.reflect.KClass
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

data class Retry(
    private val number: Int = 3,
    private val backoffMultiplier: Int = 1,
    private val timeout: Duration = 3.seconds,
) {
    var currentAttemptCount = 1
        private set
    var currentWaitTimeSeconds = timeout.inWholeSeconds.toInt()
        private set

    val isDefaultState: Boolean
        get() = currentAttemptCount == 1 && currentWaitTimeSeconds == timeout.inWholeSeconds.toInt()

    suspend fun canRetry() = (currentAttemptCount <= number).also {
        if (!it) return@also // Avoid unnecessary thread-blocking
        delay(currentWaitTimeSeconds.seconds)
        currentWaitTimeSeconds += (currentWaitTimeSeconds * backoffMultiplier)
        currentAttemptCount++
    }

    companion object {
        val DEFAULT_RETRYABLE_ERROR_CLASSES: Map<KClass<out Throwable>, Int> = mapOf(
            ConnectException::class to R.string.lib_error_connecting_to_server,
            SocketTimeoutException::class to R.string.lib_error_server_connection_timeout,
        )
    }
}
