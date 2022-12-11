package co.ke.xently.shopping.features.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

object ListItemSurface {
    @Composable
    operator fun invoke(
        modifier: Modifier,
        paddingValues: PaddingValues = PaddingValues(start = 16.dp, top = 8.dp, bottom = 8.dp),
        onClick: () -> Unit = {},
        content: @Composable (RowScope.() -> Unit),
    ) {
        Surface(modifier = modifier, onClick = onClick) {
            Box(modifier = Modifier.padding(paddingValues = paddingValues)) {
                Row(
                    content = content,
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                )
            }
        }
    }
}