package co.ke.xently.shopping.features.ui

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*

data class OptionMenu(
    val title: String,
    val onClick: () -> Unit = {},
)

@Composable
fun RowScope.OverflowOptionMenu(menu: List<OptionMenu>, contentDescription: String? = null) {
    var showOptionsMenu by remember {
        mutableStateOf(false)
    }
    apply {
        IconButton(onClick = { showOptionsMenu = !showOptionsMenu }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = contentDescription,
            )
        }
        DropdownMenu(
            expanded = showOptionsMenu,
            onDismissRequest = { showOptionsMenu = false },
        ) {
            for (menuItem in menu) {
                DropdownMenuItem(
                    onClick = {
                        showOptionsMenu = false
                        menuItem.onClick.invoke()
                    },
                    text = {
                        Text(menuItem.title)
                    },
                )
            }
        }
    }
}