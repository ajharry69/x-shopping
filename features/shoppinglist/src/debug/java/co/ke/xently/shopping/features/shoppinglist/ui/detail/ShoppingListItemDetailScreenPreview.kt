package co.ke.xently.shopping.features.shoppinglist.ui.detail

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import co.ke.xently.shopping.features.ui.XentlyPreview
import co.ke.xently.shopping.features.ui.theme.XentlyTheme
import co.ke.xently.shopping.features.utils.State

@SuppressLint("VisibleForTests")
@XentlyPreview
@Composable
private fun ShoppingListItemDetailScreen() {
    XentlyTheme {
        ShoppingListItemDetailScreen(
            modifier = Modifier.fillMaxSize(),
            detailState = State.Success(null),
            saveState = State.Success(null),
            config = ShoppingListItemDetailScreen.Config(),
        )
    }
}