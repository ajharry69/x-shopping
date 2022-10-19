package co.ke.xently.shopping.features.utils

import android.content.Context
import co.ke.xently.shopping.libraries.data.source.remote.ExceptionUtils.getErrorMessage
import timber.log.Timber

sealed class ListState<out T> {
    object Loading : ListState<Nothing>()
    data class Success<T>(val data: List<T>) : ListState<T>()
    data class Error(val error: Throwable) : ListState<Nothing>(), ErrorState {
        init {
            Timber.e(error)
        }

        override fun getMessage(context: Context): String = error.getErrorMessage(context)
    }
}
