package co.ke.xently.shopping.features.ui

import androidx.compose.runtime.Composable
import co.ke.xently.shopping.features.ui.theme.XentlyTheme

@XentlyPreview
@Composable
private fun ListItemTrailingIconButtonPreview() {
    XentlyTheme {
        ListItemTrailingIconButton(data = "Random string", menuItems = emptySet())
    }
}