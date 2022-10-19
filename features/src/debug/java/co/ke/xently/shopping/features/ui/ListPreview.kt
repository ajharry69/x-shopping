package co.ke.xently.shopping.features.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import co.ke.xently.shopping.features.ui.theme.XentlyTheme

@XentlyPreview
@Composable
private fun FullscreenEmptyListPreview() {
    XentlyTheme {
        FullscreenEmptyList<String>(Modifier.fillMaxSize())
    }
}