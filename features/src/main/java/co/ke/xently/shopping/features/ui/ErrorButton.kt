package co.ke.xently.shopping.features.ui

import android.content.Intent
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toUpperCase
import androidx.core.net.toUri
import co.ke.xently.shopping.features.R
import co.ke.xently.shopping.features.utils.Deeplinks
import co.ke.xently.shopping.libraries.data.source.remote.HttpException.Companion.requiresAuthentication
import co.ke.xently.shopping.libraries.data.source.utils.RetryError

object ErrorButton {
    @Composable
    operator fun invoke(
        modifier: Modifier = Modifier,
        error: Throwable? = null,
        errorButtonLabel: String? = null,
        showDefaultErrorButton: Boolean = false,
        retryError: RetryError = RetryError(),
        onClick: ((Throwable?) -> Unit)? = null,
    ) {
        val retryButtonLabel = stringResource(R.string.retry).toUpperCase(Locale.current)
        val onClickRemembered by rememberUpdatedState(newValue = onClick)
        if (error == null) {
            Button(modifier = modifier, onClick = { onClickRemembered?.invoke(null) }) {
                Text(
                    style = MaterialTheme.typography.labelLarge,
                    text = errorButtonLabel ?: retryButtonLabel,
                )
            }
        } else {
            if (error.requiresAuthentication()) {
                val context = LocalContext.current
                val defaultSignInClickAction: () -> Unit = {
                    Intent(Intent.ACTION_VIEW, Deeplinks.SIGN_IN.toUri()).also {
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
                    Text(style = MaterialTheme.typography.labelLarge, text = retryButtonLabel)
                }
            } else if (showDefaultErrorButton) {
                Button(modifier = modifier, onClick = { onClickRemembered?.invoke(null) }) {
                    Text(
                        style = MaterialTheme.typography.labelLarge,
                        text = errorButtonLabel ?: retryButtonLabel,
                    )
                }
            }
        }
    }
}