package co.ke.xently.shopping.features.ui

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview

@Preview(
    showBackground = true,
    group = "theme",
)
@Preview(
    showBackground = true,
    group = "theme",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Preview(
    showBackground = true,
    group = "device",
    device = Devices.TABLET,
)
@Preview(
    showBackground = true,
    group = "device",
    device = Devices.TABLET,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
annotation class XentlyPreview
