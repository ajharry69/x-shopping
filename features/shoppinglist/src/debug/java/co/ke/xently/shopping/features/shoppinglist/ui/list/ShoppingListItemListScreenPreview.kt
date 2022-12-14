package co.ke.xently.shopping.features.shoppinglist.ui.list

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import co.ke.xently.shopping.features.shoppinglist.repositories.ShoppingListGroup
import co.ke.xently.shopping.features.shoppinglist.ui.search.ShoppingListItemSearchScreen
import co.ke.xently.shopping.features.ui.XentlyPreview
import co.ke.xently.shopping.features.ui.theme.XentlyTheme
import co.ke.xently.shopping.features.utils.State
import co.ke.xently.shopping.libraries.data.source.ShoppingListItem
import kotlinx.coroutines.flow.flowOf
import java.util.*

@SuppressLint("VisibleForTests")
@XentlyPreview
@Composable
private fun ShoppingListItemListScreenPreview() {
    XentlyTheme {
        val items =
            flowOf(PagingData.from(List(20) { ShoppingListItem.DEFAULT_INSTANCE.copy(id = it.toLong()) })).collectAsLazyPagingItems()
        ShoppingListItemListScreen(
            modifier = Modifier.fillMaxSize(),
            items = items,
            removeState = State.Success(null),
            group = null,
            menuItems = emptySet(),
            config = ShoppingListItemSearchScreen.Config(),
        )
    }
}

@SuppressLint("VisibleForTests")
@XentlyPreview
@Composable
private fun ShoppingListItemListScreenWithGroupPreview() {
    XentlyTheme {
        val items =
            flowOf(PagingData.from(List(20) { ShoppingListItem.DEFAULT_INSTANCE.copy(id = it.toLong()) })).collectAsLazyPagingItems()
        ShoppingListItemListScreen(
            modifier = Modifier.fillMaxSize(),
            items = items,
            removeState = State.Success(null),
            group = ShoppingListGroup(group = Date().toString()),
            menuItems = emptySet(),
            config = ShoppingListItemSearchScreen.Config(),
        )
    }
}