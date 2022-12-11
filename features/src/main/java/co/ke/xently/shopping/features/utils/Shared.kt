package co.ke.xently.shopping.features.utils

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Stable
import co.ke.xently.shopping.libraries.data.source.User

@Stable
data class Shared(
    val user: User? = null,
    val snackbarHostState: SnackbarHostState = SnackbarHostState(),
    val onNavigationIconClicked: () -> Unit = {},
    val onAuthenticationRequired: () -> Unit = {},
    val onAuthenticationExpected: (isNotificationDismissible: Boolean) -> Unit = {},
)
