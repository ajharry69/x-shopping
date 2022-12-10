package co.ke.xently.shopping.features.ui

import android.content.Intent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toUpperCase
import androidx.core.net.toUri
import co.ke.xently.shopping.features.R
import co.ke.xently.shopping.features.utils.Routes
import co.ke.xently.shopping.libraries.data.source.remote.HttpException.Companion.requiresAuthentication
import co.ke.xently.shopping.libraries.data.source.utils.RetryError

@Composable
fun ErrorButton(
    modifier: Modifier = Modifier,
    error: Throwable? = null,
    defaultErrorButtonLabel: String = stringResource(R.string.retry).toUpperCase(Locale.current),
    showDefaultErrorButton: Boolean = false,
    retryError: RetryError = RetryError(),
    onClick: ((Throwable?) -> Unit)? = null,
) {
    val onClickRemembered by rememberUpdatedState(newValue = onClick)
    if (error == null) {
        Button(modifier = modifier, onClick = { onClickRemembered?.invoke(null) }) {
            Text(
                style = MaterialTheme.typography.labelLarge,
                text = defaultErrorButtonLabel,
            )
        }
    } else {
        if (error.requiresAuthentication()) {
            val context = LocalContext.current
            val defaultSignInClickAction: () -> Unit = {
                Intent(Intent.ACTION_VIEW, Routes.Users.Deeplinks.SIGN_IN.toUri()).also {
                    context.startActivity(it)
                }
            }
            Button(modifier = modifier,
                onClick = { onClickRemembered?.invoke(error) ?: defaultSignInClickAction() }) {
                Text(
                    style = MaterialTheme.typography.labelLarge,
                    text = stringResource(R.string.common_signin_button_text).toUpperCase(Locale.current),
                )
            }
        } else if ((error::class in retryError.retrials) || (error.cause != null && error.cause!!::class in retryError.retrials)) {
            Button(modifier = modifier, onClick = { onClickRemembered?.invoke(error) }) {
                Text(
                    style = MaterialTheme.typography.labelLarge,
                    text = stringResource(R.string.retry).toUpperCase(Locale.current),
                )
            }
        } else if (showDefaultErrorButton) {
            Button(modifier = modifier, onClick = { onClickRemembered?.invoke(null) }) {
                Text(
                    style = MaterialTheme.typography.labelLarge,
                    text = defaultErrorButtonLabel,
                )
            }
        }
    }
}

@Composable
fun PasswordVisibilityToggle(isVisible: Boolean, onClick: () -> Unit) {
    val description = stringResource(
        R.string.toggle_password_visibility,
        if (isVisible) {
            R.string.hide
        } else {
            R.string.show
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