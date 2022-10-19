package co.ke.xently.shopping.features.utils

import androidx.compose.material3.SnackbarHostState
import co.ke.xently.shopping.libraries.data.source.User

data class Shared(
    val user: User? = null,
    val snackbarHostState: SnackbarHostState = SnackbarHostState(),
    val onNavigationIconClicked: () -> Unit = {},
    val onAuthenticationRequired: () -> Unit = {},
    val onAuthenticationExpected: (isNotificationDismissible: Boolean) -> Unit = {},
)
