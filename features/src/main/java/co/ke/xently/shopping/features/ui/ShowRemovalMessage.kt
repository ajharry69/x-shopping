package co.ke.xently.shopping.features.ui

import androidx.annotation.StringRes
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import co.ke.xently.shopping.features.utils.State

object ShowRemovalMessage {
    @Composable
    operator fun invoke(
        removeState: State<Any>,
        hostState: SnackbarHostState,
        @StringRes successMessage: Int,
    ) {
        val context = LocalContext.current
        LaunchedEffect(removeState) {
            var duration = SnackbarDuration.Long
            val message = when (removeState) {
                is State.Error -> {
                    removeState.getMessage(context)
                }
                is State.Success -> {
                    if (removeState.data == null) {
                        return@LaunchedEffect
                    }
                    duration = SnackbarDuration.Short
                    context.getString(successMessage)
                }
                else -> {
                    return@LaunchedEffect
                }
            }
            hostState.showSnackbar(message, duration = duration)
        }
    }
}