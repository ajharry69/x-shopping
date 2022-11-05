package co.ke.xently.shopping.features.ui

import androidx.annotation.StringRes
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import co.ke.xently.shopping.features.R
import co.ke.xently.shopping.features.stringRes
import co.ke.xently.shopping.features.utils.State

data class DetailScreen<T>(
    val data: T?,
    val shouldResetFields: Boolean,
    val showProgressIndicator: Boolean,
) {
    @Composable
    fun title(@StringRes prefixedTitle: Int): String {
        return stringRes(
            prefixedTitle,
            if (data == null) {
                R.string.feature_add
            } else {
                R.string.feature_update
            },
        )
    }

    companion object {
        @Composable
        operator fun <T, Save> invoke(
            state: State<T>,
            saveState: State<Save>,
            snackbarHostState: SnackbarHostState,
            onAddSuccess: suspend () -> Unit,
            onUpdateSuccess: suspend () -> Unit,
        ): DetailScreen<T> {
            val data by remember(state) {
                derivedStateOf {
                    (state as? State.Success)?.data
                }
            }

            val context = LocalContext.current
            val showProgressIndicator by remember(state, saveState) {
                derivedStateOf {
                    state is State.Loading || saveState is State.Loading
                }
            }

            val saveStateData by remember(saveState) {
                derivedStateOf {
                    (saveState as? State.Success)?.data
                }
            }

            val shouldResetFields by remember(saveStateData, data) {
                derivedStateOf {
                    saveStateData != null && data == null
                }
            }

            LaunchedEffect(saveState, data) {
                if (saveState is State.Success) {
                    if (saveState.data == null) {
                        return@LaunchedEffect
                    }
                    if (data == null) {
                        onAddSuccess()
                    } else {
                        onUpdateSuccess()
                    }
                } else if (saveState is State.Error) {
                    snackbarHostState.showSnackbar(
                        duration = SnackbarDuration.Long,
                        message = saveState.getMessage(context),
                    )
                }
            }

            LaunchedEffect(state) {
                if (state is State.Error) {
                    val message = state.getMessage(context)
                    snackbarHostState.showSnackbar(
                        message = message,
                        duration = SnackbarDuration.Long,
                    )
                }
            }

            return DetailScreen(
                data = data,
                shouldResetFields = shouldResetFields,
                showProgressIndicator = showProgressIndicator,
            )
        }
    }
}
