package co.ke.xently.shopping.features.users.ui

import androidx.compose.runtime.Composable
import co.ke.xently.shopping.features.ui.XentlyPreview
import co.ke.xently.shopping.features.ui.theme.XentlyTheme


@XentlyPreview
@Composable
private fun PasswordVisibilityToggleInvisiblePreview() {
    XentlyTheme {
        PasswordVisibilityToggle(isVisible = false) {

        }
    }
}

@XentlyPreview
@Composable
private fun PasswordVisibilityToggleVisiblePreview() {
    XentlyTheme {
        PasswordVisibilityToggle(isVisible = true) {

        }
    }
}