package co.ke.xently.shopping.features.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.placeholder
import com.google.accompanist.placeholder.material.shimmer
import java.text.NumberFormat
import java.util.*

val DefaultKeyboardOptions = KeyboardOptions.Default.copy(
    imeAction = ImeAction.Next,
    capitalization = KeyboardCapitalization.Sentences,
)

fun Modifier.shimmerPlaceholder(visible: Boolean) = composed {
    placeholder(
        visible = visible,
        highlight = PlaceholderHighlight.shimmer(),
    )
}

@Composable
fun labelAsRequired(@StringRes id: Int, positionAtStart: Boolean = false): String {
    return buildString {
        if (positionAtStart) {
            append('*')
            stringResource(id)
        } else {
            stringResource(id)
            append('*')
        }
    }
}

fun Modifier.fillMaxWidthHorizontalPadding(padding: Dp = 16.dp) = fillMaxWidth()
    .padding(horizontal = padding)

@Composable
fun rememberFragmentManager(): FragmentManager {
    val context = LocalContext.current
    return remember(context) {
        (context as FragmentActivity).supportFragmentManager
    }
}

@Composable
fun rememberCurrencyFormat(
    locale: Locale? = null,
    currency: Currency? = null,
    configureFormat: NumberFormat.() -> Unit = {},
): NumberFormat {
    val currentConfigureFormat by rememberUpdatedState(configureFormat)
    val numberFormatter = remember(locale, currency) {
        NumberFormat.getCurrencyInstance(locale ?: Locale.getDefault()).apply {
            this.currency = currency ?: Currency.getInstance("KES")
            currentConfigureFormat()
        }
    }
    return numberFormatter
}

@Composable
fun rememberNumberFormat(
    locale: Locale? = null,
    currency: Currency? = null,
    configureFormat: NumberFormat.() -> Unit = {},
): NumberFormat {
    val currentConfigureFormat by rememberUpdatedState(configureFormat)
    val numberFormatter = remember(locale, currency) {
        NumberFormat.getNumberInstance(locale ?: Locale.getDefault()).apply {
            this.currency = currency ?: Currency.getInstance("KES")
            currentConfigureFormat()
        }
    }
    return numberFormatter
}

@Composable
fun CallOnLifecycleEvent(onEvent: (Lifecycle.Event) -> Unit) {
    val currentOnEvent by rememberUpdatedState(onEvent)
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            currentOnEvent(event)
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}