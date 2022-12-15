package co.ke.xently.shopping.features.recommendation.ui.request

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import co.ke.xently.shopping.features.ui.XentlyPreview
import co.ke.xently.shopping.features.ui.theme.XentlyTheme
import co.ke.xently.shopping.features.utils.Shared
import co.ke.xently.shopping.libraries.data.source.ShoppingListItem

@SuppressLint("VisibleForTests")
@XentlyPreview
@Composable
private fun RecommendationRequestScreenPreview() {
    XentlyTheme {
        RecommendationRequestScreen(
            shared = Shared(),
            modifier = Modifier.fillMaxSize(),
            unsavedShoppingList = List(5) {
                "Bread ${it + 1}"
            },
            savedShoppingList = List(3) {
                ShoppingListItem.DEFAULT_INSTANCE.copy(id = (it + 1).toLong())
            },
        )
    }
}