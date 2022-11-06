package co.ke.xently.shopping.features.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import co.ke.xently.shopping.features.R
import co.ke.xently.shopping.features.utils.Shared

object MoveBackNavigationIconButton {
    @Composable
    operator fun invoke(onClick: () -> Unit) {
        IconButton(onClick = onClick) {
            Icon(Icons.Default.ArrowBack, stringResource(R.string.content_description_move_back))
        }
    }

    @Composable
    operator fun invoke(shared: Shared) {
        invoke(shared.onNavigationIconClicked)
    }
}