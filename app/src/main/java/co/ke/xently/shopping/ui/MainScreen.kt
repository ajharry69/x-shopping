package co.ke.xently.shopping.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import co.ke.xently.shopping.MainActivityViewModel
import co.ke.xently.shopping.MainNavGraph
import co.ke.xently.shopping.R
import co.ke.xently.shopping.features.products.ui.detail.destinations.ProductDetailScreenDestination
import co.ke.xently.shopping.features.recommendation.ui.destinations.RecommendationRequestScreenDestination
import co.ke.xently.shopping.features.shoppinglist.ShoppingListNavigator
import co.ke.xently.shopping.features.shoppinglist.ui.list.grouped.GroupedShoppingListScreen.GroupedShoppingListScreen
import co.ke.xently.shopping.features.shoppinglist.ui.list.grouped.GroupedShoppingListViewModel
import co.ke.xently.shopping.features.shops.ui.detail.destinations.ShopDetailScreenDestination
import co.ke.xently.shopping.features.users.ui.destinations.PasswordResetScreenDestination
import co.ke.xently.shopping.features.users.ui.destinations.VerificationScreenDestination
import co.ke.xently.shopping.features.utils.Shared
import com.ramcosta.composedestinations.annotation.Destination
import kotlinx.coroutines.launch

@Stable
internal data class Item(
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

@MainNavGraph
@Destination
@Composable
internal fun MainScreen(
    shared: Shared,
    navigator: ShoppingListNavigator,
    viewModel: MainActivityViewModel,
    gslViewModel: GroupedShoppingListViewModel,
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val user by remember(shared.user) {
        derivedStateOf {
            shared.user
        }
    }

    val (userName, userEmail) = remember(user) {
        (user?.name?.ifBlank { null }
            ?: "Anonymous") to (user?.email?.ifBlank { null }
            ?: "anonymous@example.com")
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
        if (!user!!.isVerified) {
            val result = shared.snackbarHostState.showSnackbar(
                duration = SnackbarDuration.Indefinite,
                actionLabel = context.getString(R.string.verify).uppercase(),
                message = context.getString(R.string.account_not_verified_verify),
            )
            if (result == SnackbarResult.ActionPerformed) {
                navigator.navigate(VerificationScreenDestination()) {
                    launchSingleTop = true
                }
            }
        } else if (user!!.isPasswordResetRequested) {
            val result = shared.snackbarHostState.showSnackbar(
                withDismissAction = true,
                duration = SnackbarDuration.Indefinite,
                message = context.getString(R.string.pending_password_reset_request),
                actionLabel = context.getString(R.string.continue_password_reset).uppercase(),
            )
            when (result) {
                SnackbarResult.ActionPerformed -> {
                    navigator.navigate(PasswordResetScreenDestination()) {
                        launchSingleTop = true
                    }
                }
                SnackbarResult.Dismissed -> {
                    viewModel.signOut()
                }
            }
        }
    }

    val items = remember {
        mutableListOf(
            Item(
                logo = Icons.Default.AddBusiness,
                title = context.getString(R.string.dashboard_item_add_shop),
                onClick = {
                    navigator.navigate(ShopDetailScreenDestination()) {
                        launchSingleTop = true
                    }
                },
            ),
            Item(
                logo = Icons.Default.AddTask,
                title = context.getString(R.string.dashboard_item_add_product),
                onClick = {
                    navigator.navigate(ProductDetailScreenDestination()) {
                        launchSingleTop = true
                    }
                },
            ),
            Item(
                logo = Icons.Default.Recommend,
                title = context.getString(R.string.dashboard_item_recommend_shops),
                onClick = {
                    navigator.navigate(RecommendationRequestScreenDestination()) {
                        launchSingleTop = true
                    }
                },
            ),
            Item(
                logo = Icons.Default.Logout,
                onClick = viewModel::signOut,
                title = context.getString(R.string.dashboard_item_logout),
            ),
        )
    }
    val selectedItem = remember { mutableStateOf(items[0]) }

    DismissibleNavigationDrawer(
        modifier = Modifier.fillMaxSize(),
        drawerState = drawerState,
        content = {
            GroupedShoppingListScreen(
                navigator = navigator,
                viewModel = gslViewModel,
                shared = shared.copy(
                    onNavigationIconClicked = {
                        scope.launch {
                            if (drawerState.isOpen) {
                                drawerState.close()
                            } else if (drawerState.isClosed) {
                                drawerState.open()
                            }
                        }
                    }
                ),
            )
        },
        drawerContent = {
            DismissibleDrawerSheet(drawerTonalElevation = 10.dp) {
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
