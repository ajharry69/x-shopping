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

internal object RecommendationRequestScreen {
    @Stable
    data class Config(
        val shared: Shared = Shared(),
        val onRecommendClick: () -> Unit = {},
        val addUnsavedItem: (String) -> Unit = {},
        val removeUnsavedItem: (String) -> Unit = {},
        val restoreRemovedSavedItems: () -> Unit = {},
        val lookupItemToBeAdded: (String) -> Unit = {},
        val restoreRemovedUnsavedItems: () -> Unit = {},
        val removeSavedItem: (ShoppingListItem) -> Unit = {},
        val onUpdateSuccess: () -> Unit = shared.onNavigationIconClicked,
    )

    @RecommendationNavGraph(start = true)
    @Destination
    @Composable
    fun RecommendationRequestScreen(
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
            }
        }

        RecommendationRequestScreen(
            modifier = Modifier.fillMaxSize(),
            savedShoppingList = savedShoppingList,
            unsavedShoppingList = unsavedShoppingList,
            hasSavedShoppingListItemInTheRecycleBin = hasSavedShoppingListItemInTheRecycleBin,
            itemToBeAddedExistsInUnsavedShoppingList = itemToBeAddedExistsInUnsavedShoppingList,
            hasUnsavedShoppingListItemInTheRecycleBin = hasUnsavedShoppingListItemInTheRecycleBin,
            config = Config(
                shared = shared,
                addUnsavedItem = viewModel::addUnsavedShoppingList,
                removeSavedItem = viewModel::removeSavedShoppingList,
                lookupItemToBeAdded = viewModel::lookupItemToBeAdded,
                removeUnsavedItem = viewModel::removeUnsavedShoppingList,
                restoreRemovedSavedItems = viewModel::restoreRemovedSavedItems,
                restoreRemovedUnsavedItems = viewModel::restoreRemovedUnsavedItems,
                onRecommendClick = {
                    navigator.navigate(RecommendationScreenDestination()) {
                        launchSingleTop = true
                    }
                },
            ),
        )
    }


    @Composable
    @VisibleForTesting
    operator fun invoke(
        config: Config,
        modifier: Modifier,
        unsavedShoppingList: List<String>,
        savedShoppingList: List<ShoppingListItem>,
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

        Scaffold(
            topBar = {
                TopAppBarWithProgressIndicator {
                    TopAppBar(
                        title = {
                            Text(stringResource(R.string.toolbar_title_request_recommendation))
                        },
                        navigationIcon = {
                            MoveBackNavigationIconButton(config.shared)
                        },
                    )
                }
            },
            snackbarHost = {
                SnackbarHost(hostState = config.shared.snackbarHostState)
            },
        ) { values: PaddingValues ->
            LazyColumn(
                modifier = modifier
                    .padding(values)
                    .safeContentPadding(),
            ) {
                stickyHeader {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier
                            .fillMaxWidthHorizontalPadding()
                            .padding(top = 8.dp),
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
                                config.lookupItemToBeAdded(it.text.trim())
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
                                        config.addUnsavedItem(name.value.text.trim())
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
                                        config.addUnsavedItem(name.value.text.trim())
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
                                config.onRecommendClick()
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

                if (hasUnsavedShoppingListItemInTheRecycleBin || unsavedShoppingList.isNotEmpty()) {
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
                                    onClick = config.restoreRemovedUnsavedItems,
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
                                IconButton(onClick = { config.removeUnsavedItem(it) }) {
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

                if (hasSavedShoppingListItemInTheRecycleBin || savedShoppingList.isNotEmpty()) {
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
                                    onClick = config.restoreRemovedSavedItems,
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
                                    IconButton(onClick = { config.removeSavedItem(it) }) {
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
