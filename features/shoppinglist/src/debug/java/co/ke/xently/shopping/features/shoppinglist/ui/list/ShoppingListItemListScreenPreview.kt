package co.ke.xently.shopping.features.shoppinglist.ui.list

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import co.ke.xently.shopping.features.shoppinglist.ui.search.ShoppingListItemSearchScreen
import co.ke.xently.shopping.features.ui.XentlyPreview
import co.ke.xently.shopping.features.ui.theme.XentlyTheme
import co.ke.xently.shopping.features.utils.State
import co.ke.xently.shopping.libraries.data.source.ShoppingListItem
import kotlinx.coroutines.flow.flowOf

@SuppressLint("VisibleForTests")
@XentlyPreview
@Composable
private fun ShoppingListItemListScreenPreview() {
    XentlyTheme {
        val items =
            flowOf(PagingData.from(List(20) { ShoppingListItem.DEFAULT_INSTANCE.copy(id = it.toLong()) })).collectAsLazyPagingItems()
        ShoppingListItemListScreen(
            modifier = Modifier.fillMaxSize(),
            config = ShoppingListItemSearchScreen.Config(),
            items = items,
            removeState = State.Success(null),
            isRefreshing = false,
            menuItems = emptySet(),
        )
    }
}