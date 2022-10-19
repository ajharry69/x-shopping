package co.ke.xently.shopping.libraries.data.source.remote

import android.content.Context
import co.ke.xently.shopping.libraries.data.source.utils.Retry
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.retry
import kotlin.reflect.KClass

object ExceptionUtils {
    fun Throwable.getErrorMessage(
        context: Context,
        preferredErrorLookups: Map<KClass<out Throwable>, Int> = emptyMap(),
    ): String {
        return (Retry.DEFAULT_RETRYABLE_ERROR_CLASSES + preferredErrorLookups)[this::class]
            ?.let(context::getString)
            ?: localizedMessage
            ?: message
            ?: context.getString(R.string.lib_generic_error_message)
    }

    fun <T> Flow<Result<T>>.retryCatch(retry: Retry) = retry {
        it::class in Retry.DEFAULT_RETRYABLE_ERROR_CLASSES && retry.canRetry()
    }.catch {
        val error = when {
            it is CancellationException -> {
                throw it
            }
            it::class in Retry.DEFAULT_RETRYABLE_ERROR_CLASSES -> {
                HttpException(Retry.DEFAULT_RETRYABLE_ERROR_CLASSES[it::class],
                    HttpException.ERROR_CODE_IO_ERROR).apply {
                    initCause(it)
                }
            }
            else -> {
                it
            }
        }
        emit(Result.failure(error))
    }
}