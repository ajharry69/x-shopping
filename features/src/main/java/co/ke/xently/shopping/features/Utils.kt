package co.ke.xently.shopping.features

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

val Context.fileFriendlyAppName
    get() = getString(R.string.app_name).replace("\\s+".toRegex(), "").lowercase()

@Composable
fun stringRes(@StringRes string: Int, @StringRes vararg args: Int): String {
    return stringResource(string, *args.map {
        stringResource(it)
    }.toTypedArray())
}
