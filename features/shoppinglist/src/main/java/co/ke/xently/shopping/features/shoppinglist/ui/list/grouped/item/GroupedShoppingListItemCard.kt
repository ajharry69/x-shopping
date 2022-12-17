package co.ke.xently.shopping.features.shoppinglist.ui.list.grouped.item

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import co.ke.xently.shopping.features.shoppinglist.GroupBy
import co.ke.xently.shopping.features.shoppinglist.R
import co.ke.xently.shopping.features.shoppinglist.repositories.ShoppingListGroup
import co.ke.xently.shopping.features.shoppinglist.ui.list.item.ShoppingListItemListItem
import co.ke.xently.shopping.features.ui.shimmerPlaceholder
import co.ke.xently.shopping.libraries.data.source.GroupedShoppingList

object GroupedShoppingListItemCard {
    @Stable
    data class MenuItem(
        @StringRes
        val label: Int,
        val onClick: (GroupedShoppingList) -> Unit,
    )

    @Composable
    internal operator fun invoke(
        groupList: GroupedShoppingList,
        listCount: Map<Any, Int>,
        modifier: Modifier = Modifier,
        showPlaceholder: Boolean = false,
        groupBy: GroupBy = GroupBy.DateAdded,
        groupMenuItems: Set<MenuItem> = emptySet(),
        menuItems: Set<ShoppingListItemListItem.MenuItem> = emptySet(),
        onSeeAllClicked: (ShoppingListGroup) -> Unit = {},
    ) {
        val itemsPerCard = 3
        var showDropDownMenu by remember {
            mutableStateOf(false)
        }
        val numberOfItems = remember(groupList) {
            listCount.getOrElse(groupList.group) {
                groupList.numberOfItems
            }
        }

        ElevatedCard(modifier = modifier) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(
                        modifier = Modifier.padding(bottom = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(
                            text = groupList.group,
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.shimmerPlaceholder(showPlaceholder),
                        )
                        Text(
                            text = LocalContext.current.resources.getQuantityString(
                                R.plurals.feature_shoppinglist_group_items_count,
                                numberOfItems, numberOfItems
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.shimmerPlaceholder(showPlaceholder),
                        )
                    }
                    Box(modifier = Modifier.align(Alignment.Top)) {
                        IconButton(onClick = { showDropDownMenu = !showPlaceholder }) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = stringResource(
                                    R.string.feature_shoppinglist_list_item_drop_down_menu_content_description,
                                    groupList.group,
                                ),
                                modifier = Modifier.shimmerPlaceholder(showPlaceholder),
                            )
                        }
                        DropdownMenu(
                            expanded = showDropDownMenu,
                            onDismissRequest = { showDropDownMenu = false },
                        ) {
                            for (item in groupMenuItems) {
                                DropdownMenuItem(
                                    onClick = {
                                        item.onClick(groupList)
                                        showDropDownMenu = false
                                    },
                                    text = {
                                        Text(text = stringResource(item.label))
                                    },
                                )
                            }
                        }
                    }
                }
                Divider(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                    thickness = 1.dp,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .shimmerPlaceholder(showPlaceholder),
                )
                Column {
                    for (item in groupList.shoppingList.take(itemsPerCard)) {
                        ShoppingListItemListItem(
                            item = item,
                            menuItems = menuItems,
                            showPlaceholder = showPlaceholder,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
                AnimatedVisibility(visible = numberOfItems > itemsPerCard) {
                    OutlinedButton(
                        onClick = {
                            if (!showPlaceholder) {
                                onSeeAllClicked(
                                    ShoppingListGroup(
                                        groupBy = groupBy,
                                        group = groupList.group,
                                    ),
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .shimmerPlaceholder(showPlaceholder)
                            .padding(horizontal = 16.dp)
                    ) {
                        Text(
                            stringResource(R.string.feature_shoppinglist_group_button_see_all),
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
            }
        }
    }
}