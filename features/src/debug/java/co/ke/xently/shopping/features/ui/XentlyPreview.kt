package co.ke.xently.shopping.features.ui

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview

@Preview(
    showBackground = true,
    group = "theme",
    name = "Light theme",
)
@Preview(
    showBackground = true,
    group = "theme",
    name = "Dark theme",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Preview(
    showBackground = true,
    group = "device",
    device = Devices.TABLET,
    name = "Tablet light theme",
)
@Preview(
    showBackground = true,
    group = "device",
    device = Devices.TABLET,
    name = "Tablet dark theme",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
annotation class XentlyPreview
