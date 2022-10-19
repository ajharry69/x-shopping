package co.ke.xently.shopping.features.users.ui.password.reset

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import co.ke.xently.shopping.features.ui.XentlyPreview
import co.ke.xently.shopping.features.ui.theme.XentlyTheme
import co.ke.xently.shopping.features.utils.State

@SuppressLint("VisibleForTests")
@XentlyPreview
@Composable
private fun PasswordResetScreenPreview() {
    XentlyTheme {
        PasswordResetScreen(
            modifier = Modifier.fillMaxSize(),
            passwordResetState = State.Success(null),
            config = PasswordResetScreen.Config(),
            isChange = false,
        )
    }
}