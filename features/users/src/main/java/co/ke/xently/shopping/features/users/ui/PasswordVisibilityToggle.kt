package co.ke.xently.shopping.features.users.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import co.ke.xently.shopping.features.users.R

object PasswordVisibilityToggle {
    @Composable
    operator fun invoke(isVisible: Boolean, onClick: () -> Unit) {
        val description = stringResource(
            R.string.fusers_toggle_password_visibility,
            if (isVisible) {
                R.string.fusers_hide
            } else {
                R.string.fusers_show
            },
        )
        IconButton(onClick, modifier = Modifier.semantics { testTag = description }) {
            Icon(
                if (isVisible) {
                    Icons.Default.VisibilityOff
                } else {
                    Icons.Default.Visibility
                },
                contentDescription = description,
            )
        }
    }
}