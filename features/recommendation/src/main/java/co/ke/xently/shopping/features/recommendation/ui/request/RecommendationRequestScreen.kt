package co.ke.xently.shopping.features.recommendation.ui.request

import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import co.ke.xently.shopping.features.recommendation.R
import co.ke.xently.shopping.features.recommendation.RecommendationNavGraph
import co.ke.xently.shopping.features.recommendation.RecommendationNavigator
import co.ke.xently.shopping.features.recommendation.ui.destinations.RecommendationScreenDestination
import co.ke.xently.shopping.features.ui.*
import co.ke.xently.shopping.features.utils.Shared
import co.ke.xently.shopping.libraries.data.source.ShoppingListItem
import com.ramcosta.composedestinations.annotation.Destination

object RecommendationRequestScreen {
    data class Args(val items: Array<ShoppingListItem> = emptyArray()) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Args

            if (!items.contentEquals(other.items)) return false

            return true
        }

        override fun hashCode(): Int {
            return items.contentHashCode()
        }
    }

    @RecommendationNavGraph(start = true)
    @Destination(navArgsDelegate = Args::class)
    @Composable
    internal fun RecommendationRequestScreen(
        args: Args,
        shared: Shared,
        navigator: RecommendationNavigator,
        viewModel: RecommendationRequestViewModel = hiltViewModel(),
    ) {
        val savedShoppingList by viewModel.savedShoppingList.collectAsState()
        val unsavedShoppingList by viewModel.unsavedShoppingList.collectAsState()
        val hasSavedShoppingListItemInTheRecycleBin by viewModel.hasSavedShoppingListItemInTheRecycleBin.collectAsState()
        val hasUnsavedShoppingListItemInTheRecycleBin by viewModel.hasUnsavedShoppingListItemInTheRecycleBin.collectAsState()
        val itemToBeAddedExistsInUnsavedShoppingList by viewModel.itemToBeAddedExistsInUnsavedShoppingList.collectAsState()

        CallOnLifecycleEvent {
            if (it == Lifecycle.Event.ON_DESTROY) {
                viewModel.clean()
            } else if (it == Lifecycle.Event.ON_CREATE) {
                args.items.forEach(viewModel::addSavedShoppingList)
            }
        }

        RecommendationRequestScreen(
            shared = shared,
            modifier = Modifier.fillMaxSize(),
            savedShoppingList = savedShoppingList,
            unsavedShoppingList = unsavedShoppingList,
            addUnsavedItem = viewModel::addUnsavedShoppingList,
            removeSavedItem = viewModel::removeSavedShoppingList,
            lookupItemToBeAdded = viewModel::lookupItemToBeAdded,
            removeUnsavedItem = viewModel::removeUnsavedShoppingList,
            restoreRemovedSavedItems = viewModel::restoreRemovedSavedItems,
            restoreRemovedUnsavedItems = viewModel::restoreRemovedUnsavedItems,
            hasSavedShoppingListItemInTheRecycleBin = hasSavedShoppingListItemInTheRecycleBin,
            itemToBeAddedExistsInUnsavedShoppingList = itemToBeAddedExistsInUnsavedShoppingList,
            hasUnsavedShoppingListItemInTheRecycleBin = hasUnsavedShoppingListItemInTheRecycleBin,
            onRecommendClick = {
                navigator.navigate(RecommendationScreenDestination()) {
                    launchSingleTop = true
                }
            },
        )
    }

    @Composable
    @VisibleForTesting
    internal operator fun invoke(
        shared: Shared,
        modifier: Modifier,
        unsavedShoppingList: List<String>,
        savedShoppingList: List<ShoppingListItem>,
        onRecommendClick: () -> Unit = {},
        addUnsavedItem: (String) -> Unit = {},
        removeUnsavedItem: (String) -> Unit = {},
        restoreRemovedSavedItems: () -> Unit = {},
        lookupItemToBeAdded: (String) -> Unit = {},
        restoreRemovedUnsavedItems: () -> Unit = {},
        removeSavedItem: (ShoppingListItem) -> Unit = {},
        hasSavedShoppingListItemInTheRecycleBin: Boolean = false,
        itemToBeAddedExistsInUnsavedShoppingList: Boolean = false,
        hasUnsavedShoppingListItemInTheRecycleBin: Boolean = false,
    ) {
        val context = LocalContext.current
        val focusManager = LocalFocusManager.current
        val ungroupedNumberFormat = rememberNumberFormat {
            isGroupingUsed = true
        }

        val enableRecommendButton by remember(unsavedShoppingList, savedShoppingList) {
            derivedStateOf {
                unsavedShoppingList.isNotEmpty() || savedShoppingList.isNotEmpty()
            }
        }

        val showSavedShoppingListUI by remember(
            savedShoppingList,
            hasSavedShoppingListItemInTheRecycleBin,
        ) {
            derivedStateOf {
                hasSavedShoppingListItemInTheRecycleBin || savedShoppingList.isNotEmpty()
            }
        }

        val showUnsavedShoppingListUI by remember(
            unsavedShoppingList,
            hasUnsavedShoppingListItemInTheRecycleBin,
        ) {
            derivedStateOf {
                hasUnsavedShoppingListItemInTheRecycleBin || unsavedShoppingList.isNotEmpty()
            }
        }

        val showEmptyListUI by remember(showSavedShoppingListUI, showUnsavedShoppingListUI) {
            derivedStateOf {
                !showSavedShoppingListUI && !showUnsavedShoppingListUI
            }
        }
        Scaffold(
            topBar = {
                TopAppBarWithProgressIndicator {
                    TopAppBar(
                        title = {
                            Text(stringResource(R.string.toolbar_title_request_recommendation))
                        },
                        navigationIcon = {
                            MoveBackNavigationIconButton(shared)
                        },
                    )
                }
            },
            snackbarHost = {
                SnackbarHost(hostState = shared.snackbarHostState)
            },
        ) { values: PaddingValues ->
            LazyColumn(
                modifier = modifier
                    .padding(values)
                    .safeContentPadding(),
            ) {
                stickyHeader {
                    Column(if (showEmptyListUI) Modifier.fillParentMaxSize() else Modifier) {
                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.background),
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp)
                                    .padding(top = 10.dp, bottom = 4.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                            ) {
                                val name = TextFieldConfig<String>(
                                    labelId = R.string.field_label_item_name,
                                    extraErrorChecks = {
                                        if (itemToBeAddedExistsInUnsavedShoppingList) {
                                            context.getString(
                                                R.string.duplicate_unsaved_shopping_list_item,
                                                it.text.trim(),
                                            )
                                        } else null
                                    },
                                )

                                val requiredFields = arrayOf(name)
                                val enableSubmitButton by remember(*requiredFields) {
                                    derivedStateOf {
                                        requiredFields.all { !it.hasError }
                                    }
                                }

                                TextField(
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    value = name.value,
                                    isError = name.hasError,
                                    onValueChange = {
                                        name.onValueChange(it)
                                        lookupItemToBeAdded(it.text.trim())
                                    },
                                    label = {
                                        Text(name.label)
                                    },
                                    supportingText = {
                                        SupportingText(
                                            config = name,
                                            helpText = stringResource(R.string.help_text_click_to_add_unsaved_item),
                                        )
                                    },
                                    trailingIcon = {
                                        IconButton(
                                            enabled = enableSubmitButton,
                                            onClick = {
                                                addUnsavedItem(name.value.text.trim())
                                                name.onValueChange(TextFieldValue())
                                            },
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = stringResource(R.string.add_unsaved_item_description),
                                            )
                                        }
                                    },
                                    keyboardActions = KeyboardActions(
                                        onDone = {
                                            if (!name.hasError) {
                                                addUnsavedItem(name.value.text.trim())
                                                name.onValueChange(TextFieldValue())
                                            }
                                        },
                                    ),
                                )
                                Button(
                                    enabled = enableRecommendButton,
                                    modifier = Modifier.fillMaxWidth(),
                                    onClick = {
                                        focusManager.clearFocus()
                                        onRecommendClick()
                                    },
                                ) {
                                    Text(
                                        text = stringResource(R.string.button_label_get_recommendations)
                                            .toUpperCase(Locale.current),
                                        style = MaterialTheme.typography.labelLarge,
                                    )
                                }
                            }
                        }
                        if (showEmptyListUI) {
                            Fullscreen.EmptyList<String>(
                                modifier = Modifier.weight(1f),
                                error = stringResource(R.string.empty_shopping_list),
                            )
                        }
                    }
                }

                if (showUnsavedShoppingListUI) {
                    item {
                        ListItem(
                            headlineText = {
                                Text(
                                    stringResource(R.string.unsaved_shopping_list_title),
                                    style = MaterialTheme.typography.titleLarge,
                                )
                            },
                            trailingContent = {
                                IconButton(
                                    onClick = restoreRemovedUnsavedItems,
                                    enabled = hasUnsavedShoppingListItemInTheRecycleBin,
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Restore,
                                        contentDescription = stringResource(R.string.restore_unsaved_shopping_list),
                                    )
                                }
                            },
                        )
                    }
                    items(unsavedShoppingList, key = String::toString) {
                        ListItem(
                            headlineText = {
                                Text(it)
                            },
                            trailingContent = {
                                IconButton(onClick = { removeUnsavedItem(it) }) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteForever,
                                        contentDescription = stringResource(
                                            R.string.delete_unsaved_shopping_list_description,
                                            it,
                                        ),
                                    )
                                }
                            },
                        )
                    }
                }

                if (showSavedShoppingListUI) {
                    item {
                        ListItem(
                            headlineText = {
                                Text(
                                    stringResource(R.string.saved_shopping_list_title),
                                    style = MaterialTheme.typography.titleLarge,
                                )
                            },
                            trailingContent = {
                                IconButton(
                                    onClick = restoreRemovedSavedItems,
                                    enabled = hasSavedShoppingListItemInTheRecycleBin,
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Restore,
                                        contentDescription = stringResource(R.string.restore_saved_shopping_list),
                                    )
                                }
                            },
                        )
                    }
                    items(savedShoppingList, key = { it.id }) {
                        ListItem(
                            headlineText = {
                                Text(it.name)
                            },
                            supportingText = {
                                Text("${it.unitQuantity.let(ungroupedNumberFormat::format)} ${it.unit}")
                            },
                            trailingContent = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        it.purchaseQuantity.let(ungroupedNumberFormat::format),
                                        style = MaterialTheme.typography.titleLarge,
                                    )
                                    IconButton(onClick = { removeSavedItem(it) }) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteForever,
                                            contentDescription = stringResource(
                                                R.string.delete_saved_shopping_list_description,
                                                it.name,
                                            ),
                                        )
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
