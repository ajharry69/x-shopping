package co.ke.xently.shopping.features.shoppinglist.ui.list.item

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import co.ke.xently.shopping.features.shoppinglist.R
import co.ke.xently.shopping.features.ui.ListItemSurface
import co.ke.xently.shopping.features.ui.rememberNumberFormat
import co.ke.xently.shopping.features.ui.shimmerPlaceholder
import co.ke.xently.shopping.libraries.data.source.ShoppingListItem

internal object ShoppingListItemListItem {
    data class MenuItem(
        @StringRes
        val label: Int,
        val onClick: (ShoppingListItem) -> Unit,
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as MenuItem

            if (label != other.label) return false

            return true
        }

        override fun hashCode(): Int {
            return label
        }

        companion object {
            fun deleteMenuItem(onDeleteClick: (ShoppingListItem) -> Unit) = MenuItem(
                onClick = onDeleteClick,
                label = R.string.feature_shoppinglist_list_item_drop_down_menu_delete,
            )
        }
    }

    @Composable
    operator fun invoke(
        modifier: Modifier,
        shoppingListItem: ShoppingListItem,
        showPlaceholder: Boolean,
        menuItems: Set<MenuItem>,
        trailingIcon: (@Composable () -> Unit)? = null,
    ) {
        val ungroupedNumberFormat = rememberNumberFormat {
            isGroupingUsed = true
        }
        ListItemSurface(modifier = modifier) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = shoppingListItem.name,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .wrapContentWidth()
                        .shimmerPlaceholder(showPlaceholder),
                )
                Text(
                    text = "${shoppingListItem.unitQuantity} ${shoppingListItem.unit}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.shimmerPlaceholder(showPlaceholder),
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = ungroupedNumberFormat.format(shoppingListItem.purchaseQuantity),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.shimmerPlaceholder(showPlaceholder),
                )
                if (trailingIcon == null) {
                    Box {
                        var showDropDownMenu by remember { mutableStateOf(false) }
                        IconButton(onClick = { showDropDownMenu = !showPlaceholder }) {
                            Icon(
                                imageVector = if (showDropDownMenu) {
                                    Icons.Default.KeyboardArrowDown
                                } else {
                                    Icons.Default.KeyboardArrowRight
                                },
                                contentDescription = stringResource(
                                    R.string.feature_shoppinglist_list_item_drop_down_menu_content_description,
                                    shoppingListItem.name,
                                ),
                                modifier = Modifier.shimmerPlaceholder(showPlaceholder),
                            )
                        }
                        DropdownMenu(
                            expanded = showDropDownMenu,
                            onDismissRequest = { showDropDownMenu = false },
                        ) {
                            for (menuItem in menuItems) {
                                DropdownMenuItem(
                                    onClick = {
                                        menuItem.onClick(shoppingListItem)
                                        showDropDownMenu = false
                                    },
                                    text = {
                                        Text(stringResource(menuItem.label))
                                    },
                                )
                            }
                        }
                    }
                } else {
                    trailingIcon.invoke()
                }
            }
        }
    }
}