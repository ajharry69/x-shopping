package co.ke.xently.shopping.features.customers.ui.list

import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import co.ke.xently.shopping.features.customers.R
import co.ke.xently.shopping.features.customers.ui.CustomerListViewModel
import co.ke.xently.shopping.features.customers.ui.list.item.CustomerListItem
import co.ke.xently.shopping.features.customers.ui.search.CustomerSearchScreen
import co.ke.xently.shopping.features.customers.ui.search.CustomerSearchScreen.Content
import co.ke.xently.shopping.features.ui.ConfirmableDelete
import co.ke.xently.shopping.features.ui.ShowRemovalMessage
import co.ke.xently.shopping.features.ui.ToolbarWithProgressbar
import co.ke.xently.shopping.features.stringRes
import co.ke.xently.shopping.features.utils.ListState
import co.ke.xently.shopping.features.utils.State
import co.ke.xently.shopping.libraries.data.source.Customer

internal object CustomerListScreen {
    @Composable
    operator fun invoke(
        modifier: Modifier,
        menuItems: Set<CustomerListItem.MenuItem>,
        config: CustomerSearchScreen.Config,
        viewModel: CustomerListViewModel = hiltViewModel(),
    ) {
        val scope = rememberCoroutineScope()
        val listState: ListState<Customer> by viewModel.listState.collectAsState(
            context = scope.coroutineContext,
        )
        val removeState by viewModel.removeState.collectAsState(State.Success(null))

        CustomerListScreen(
            config = config,
            modifier = modifier,
            listState = listState,
            removeState = removeState,
            isRefreshing = false,
            menuItems = menuItems + CustomerListItem.MenuItem.deleteMenuItem(
                onDeleteClick = ConfirmableDelete {
                    viewModel.delete(it.id)
                },
            ),
        )
    }

    @Composable
    @VisibleForTesting
    operator fun invoke(
        modifier: Modifier,
        listState: ListState<Customer>,
        removeState: State<Any>,
        isRefreshing: Boolean,
        menuItems: Set<CustomerListItem.MenuItem>,
        config: CustomerSearchScreen.Config,
    ) {
        val context = LocalContext.current

        ShowRemovalMessage(
            removeState = removeState,
            hostState = config.shared.snackbarHostState,
            successMessage = R.string.fc_list_success_removing_customer,
        )

        LaunchedEffect(listState) {
            val message = if (listState is ListState.Error) {
                listState.getMessage(context)
            } else {
                return@LaunchedEffect
            }
            config.shared.snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Long,
            )
        }

        Scaffold(
            snackbarHost = {
                SnackbarHost(hostState = config.shared.snackbarHostState)
            },
            topBar = {
                ToolbarWithProgressbar(
                    title = stringResource(R.string.fc_list_toolbar_title),
                    showProgress = removeState is State.Loading,
                    onNavigationIconClicked = config.shared.onNavigationIconClicked,
                ) {
                    IconButton(onClick = config.onSearchClick) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = stringResource(R.string.fc_search_hint),
                        )
                    }
                }
            },
            floatingActionButton = {
                FloatingActionButton(onClick = config.onFabClick) {
                    Icon(
                        Icons.Default.Add,
                        stringRes(R.string.fc_detail_toolbar_title, R.string.fc_add),
                    )
                }
            },
        ) { values: PaddingValues ->
            Content(
                config = config,
                menuItems = menuItems,
                listState = listState,
                isRefreshing = isRefreshing,
                modifier = modifier.padding(values),
            )
        }
    }
}