package co.ke.xently.shopping.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import co.ke.xently.shopping.R
import co.ke.xently.shopping.libraries.data.source.User
import kotlinx.coroutines.launch

object DashboardScreen {
    data class Config(
        val onVerifyAccountRequested: () -> Unit = {},
        val onPasswordResetContinuationRequested: () -> Unit = {},
        val onPasswordResetContinuationDismissed: () -> Unit = {},
    )

    data class Item(
        val title: String,
        val logo: ImageVector,
        val enabled: Boolean = true,
        val onClick: () -> Unit = {},
    ) {
        @Composable
        operator fun invoke(modifier: Modifier) {
            val imageTint by remember(enabled) {
                derivedStateOf {
                    if (enabled) {
                        1f
                    } else {
                        0.4f
                    }
                }
            }
            ElevatedCard(
                modifier = modifier.semantics {
                    testTag = title
                },
                onClick = onClick,
                enabled = enabled,
            ) {
                Image(
                    alpha = imageTint,
                    imageVector = logo,
                    contentDescription = title,
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.CenterHorizontally)
                        .fillMaxWidth(),
                )
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    text = title,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium,
                )
            }

        }
    }

    @Composable
    operator fun invoke(
        modifier: Modifier,
        user: User?,
        config: Config,
        items: List<Item>,
        snackbarHostState: SnackbarHostState?,
        content: (@Composable (DrawerState) -> Unit),
    ) {
        val drawerState = rememberDrawerState(DrawerValue.Closed)
        val scope = rememberCoroutineScope()
        val selectedItem = remember { mutableStateOf(items[0]) }

        val hostState = snackbarHostState ?: remember {
            SnackbarHostState()
        }

        val userName: String = remember(user) {
            user?.name?.ifBlank { null }
                ?: "Anonymous"
        }

        val userEmail: String = remember(user) {
            user?.email?.ifBlank { null }
                ?: "anonymous@example.com"
        }

        val context = LocalContext.current

        BackHandler(enabled = drawerState.isOpen) {
            scope.launch {
                drawerState.close()
            }
        }

        LaunchedEffect(user) {
            if (user == null) {
                return@LaunchedEffect
            }
            if (!user.isVerified) {
                val result = hostState.showSnackbar(
                    duration = SnackbarDuration.Indefinite,
                    actionLabel = context.getString(R.string.verify).uppercase(),
                    message = context.getString(R.string.account_not_verified_verify),
                )
                if (result == SnackbarResult.ActionPerformed) {
                    config.onVerifyAccountRequested()
                }
            } else if (user.isPasswordResetRequested) {
                val result = hostState.showSnackbar(
                    withDismissAction = true,
                    duration = SnackbarDuration.Indefinite,
                    message = context.getString(R.string.pending_password_reset_request),
                    actionLabel = context.getString(R.string.continue_password_reset).uppercase(),
                )
                when (result) {
                    SnackbarResult.ActionPerformed -> {
                        config.onPasswordResetContinuationRequested()
                    }
                    SnackbarResult.Dismissed -> {
                        config.onPasswordResetContinuationDismissed()
                    }
                }
            }
        }

        DismissibleNavigationDrawer(
            modifier = modifier,
            drawerState = drawerState,
            content = {
                content(drawerState)
            },
            drawerContent = {
                DismissibleDrawerSheet {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(176.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column {
                            Image(
                                modifier = Modifier.size(100.dp),
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = stringResource(R.string.content_description_dashboard_user_profile_picture),
                            )
                            ListItem(
                                headlineText = { Text(text = userName) },
                                supportingText = { Text(text = userEmail) },
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    items.forEach { item ->
                        NavigationDrawerItem(
                            icon = { Icon(item.logo, contentDescription = null) },
                            label = { Text(item.title) },
                            selected = item == selectedItem.value,
                            onClick = {
                                scope.launch {
                                    drawerState.close()
                                    item.onClick()
                                }
                                selectedItem.value = item
                            },
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }
                }
            },
        )
    }
}