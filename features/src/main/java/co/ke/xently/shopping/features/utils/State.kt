package co.ke.xently.shopping.features.utils

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.compose.runtime.Stable
import co.ke.xently.shopping.libraries.data.source.remote.ExceptionUtils.getErrorMessage
import timber.log.Timber
import kotlin.reflect.KClass

@Stable
sealed class State<out T> {
    object Loading : State<Nothing>()
    data class Success<T>(val data: T?) : State<T>()
    data class Error(
        val error: Throwable,
        private val sqliteConstraintErrorLookups: Iterable<Pair<String, Int>> = emptyList(),
    ) : State<Nothing>(), ErrorState {
        init {
            Timber.e(error)
        }

        override fun getMessage(context: Context): String {
            val preferredErrorLookups = if (error !is SQLiteConstraintException) {
                emptyMap()
            } else {
                var map = emptyMap<KClass<out Throwable>, Int>()
                for ((columnName, errorMessage) in sqliteConstraintErrorLookups) {
                    if (error.localizedMessage?.contains(".${columnName}") == true) {
                        map = mapOf(SQLiteConstraintException::class to errorMessage)
                        break
                    }
                }
                map
            }
            return error.getErrorMessage(
                context = context,
                preferredErrorLookups = preferredErrorLookups,
            )
        }
    }
}