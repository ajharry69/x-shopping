package co.ke.xently.shopping.features.customers.ui.list.item

import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import co.ke.xently.shopping.features.customers.R
import co.ke.xently.shopping.features.ui.ListItemSurface
import co.ke.xently.shopping.features.ui.shimmerPlaceholder
import co.ke.xently.shopping.libraries.data.source.Customer

internal object CustomerListItem {
    data class MenuItem(
        @StringRes
        val label: Int,
        val onClick: (Customer) -> Unit,
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
            fun deleteMenuItem(onDeleteClick: (Customer) -> Unit) = MenuItem(
                onClick = onDeleteClick,
                label = R.string.fc_list_item_drop_down_menu_delete,
            )
        }
    }

    @Composable
    operator fun invoke(
        modifier: Modifier,
        customer: Customer,
        showPlaceholder: Boolean,
        menuItems: Set<MenuItem>,
    ) {
        ListItemSurface(modifier = modifier) {
            Image(
                modifier = Modifier.size(70.dp),
                imageVector = Icons.Default.AccountCircle,
                contentDescription = null,
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.weight(1f)) {
                Text(text = customer.name, style = MaterialTheme.typography.titleMedium)
                Row {
                    Text(text = customer.taxPin, style = MaterialTheme.typography.bodySmall)
                    if (!customer.phoneNumber.isNullOrBlank()) {
                        Text(text = " • ", style = MaterialTheme.typography.bodySmall)
                        Text(
                            text = customer.phoneNumber!!,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
                Text(
                    text = (customer.physicalAddress ?: "").ifBlank {
                        stringResource(R.string.fc_list_item_no_address)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Box(modifier = Modifier.align(Alignment.Top)) {
                var showDropDownMenu by remember { mutableStateOf(false) }
                IconButton(onClick = { showDropDownMenu = !showPlaceholder }) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = stringResource(
                            R.string.fc_list_item_drop_down_menu_content_description,
                            "${customer.name} - ${customer.taxPin}",
                        ),
                        modifier = Modifier.shimmerPlaceholder(showPlaceholder),
                    )
                }
                DropdownMenu(
                    expanded = showDropDownMenu,
                    onDismissRequest = { showDropDownMenu = false },
                ) {
                    for (item in menuItems) {
                        DropdownMenuItem(
                            onClick = {
                                item.onClick.invoke(customer)
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
    }
}