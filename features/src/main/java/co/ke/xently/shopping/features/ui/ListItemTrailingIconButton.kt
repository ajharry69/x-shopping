package co.ke.xently.shopping.features.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import co.ke.xently.shopping.features.R
import co.ke.xently.shopping.features.models.MenuItem

object ListItemTrailingIconButton {
    @Composable
    operator fun <T : Any> invoke(
        modifier: Modifier = Modifier,
        data: T,
        menuItems: Set<MenuItem<T>>,
        iconContentDescription: (T) -> String = { it.toString() },
    ) {
        var showDropDownMenu by rememberSaveable {
            mutableStateOf(false)
        }
        val rotateDegrees by animateFloatAsState(if (showDropDownMenu) 90f else 0f)
        val iconContentDescriptionRemembered by rememberUpdatedState(iconContentDescription)

        Box(modifier = modifier) {
            IconButton(onClick = { showDropDownMenu = !showDropDownMenu }) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = stringResource(
                        R.string.dropdown_menu_content_description,
                        iconContentDescriptionRemembered(data),
                    ),
                    modifier = Modifier.rotate(rotateDegrees),
                )
            }
            DropdownMenu(
                expanded = showDropDownMenu,
                onDismissRequest = { showDropDownMenu = false },
            ) {
                for (menuItem in menuItems) {
                    key(menuItem.label) {
                        DropdownMenuItem(
                            onClick = {
                                menuItem.onClick(data)
                                showDropDownMenu = false
                            },
                            text = {
                                Text(stringResource(menuItem.label))
                            },
                        )
                    }
                }
            }
        }
    }
}