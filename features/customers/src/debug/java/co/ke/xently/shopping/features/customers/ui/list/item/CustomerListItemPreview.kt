package co.ke.xently.shopping.features.customers.ui.list.item

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import co.ke.xently.shopping.features.ui.XentlyPreview
import co.ke.xently.shopping.features.ui.theme.XentlyTheme
import co.ke.xently.shopping.libraries.data.source.Customer

@XentlyPreview
@Composable
private fun CustomerListItemPreview() {
    XentlyTheme {
        CustomerListItem(
            modifier = Modifier,
            customer = Customer.DEFAULT_INSTANCE,
            showPlaceholder = true,
            menuItems = emptySet(),
        )
    }
}