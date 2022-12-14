package co.ke.xently.shopping.features.shoppinglist.ui.list.item

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import co.ke.xently.shopping.features.ui.XentlyPreview
import co.ke.xently.shopping.features.ui.theme.XentlyTheme
import co.ke.xently.shopping.libraries.data.source.ShoppingListItem


@XentlyPreview
@Composable
private fun ShoppingListItemListItemPreview() {
    XentlyTheme {
        ShoppingListItemListItem(
            modifier = Modifier,
            item = ShoppingListItem.DEFAULT_INSTANCE,
            showPlaceholder = false,
            menuItems = emptySet(),
        )
    }
}

@XentlyPreview
@Composable
private fun ShoppingListItemListItemShowPlaceholderPreview() {
    XentlyTheme {
        ShoppingListItemListItem(
            modifier = Modifier,
            item = ShoppingListItem.DEFAULT_INSTANCE,
            showPlaceholder = true,
            menuItems = emptySet(),
        )
    }
}