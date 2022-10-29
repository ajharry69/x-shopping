package co.ke.xently.shopping.features.shoppinglist.ui.detail

import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import co.ke.xently.shopping.features.shoppinglist.R
import co.ke.xently.shopping.features.ui.*
import co.ke.xently.shopping.features.utils.Shared
import co.ke.xently.shopping.features.utils.State
import co.ke.xently.shopping.libraries.data.source.ShoppingListItem

internal object ShoppingListItemDetailScreen {
    data class Config(
        val shared: Shared = Shared(),
        val onSubmitDetails: (ShoppingListItem) -> Unit = {},
        val onUpdateSuccess: () -> Unit = shared.onNavigationIconClicked,
    )

    @Composable
    operator fun invoke(
        id: Long,
        modifier: Modifier,
        config: Config,
        viewModel: ShoppingListItemDetailScreenViewModel = hiltViewModel(),
    ) {
        val detailState: State<ShoppingListItem> by viewModel.detailState.collectAsState()
        val saveState by viewModel.saveState.collectAsState(State.Success(null))
        LaunchedEffect(id) {
            viewModel.get(id)
        }
        ShoppingListItemDetailScreen(
            modifier = modifier,
            saveState = saveState,
            detailState = detailState,
            config = config.copy(
                onSubmitDetails = viewModel::save,
            ),
        )
    }


    @Composable
    @VisibleForTesting
    operator fun invoke(
        config: Config,
        modifier: Modifier,
        saveState: State<String>,
        detailState: State<ShoppingListItem>,
    ) {
        val shoppingListItem by remember(detailState) {
            derivedStateOf {
                (detailState as? State.Success)?.data
            }
        }
        val toolbarTitle = stringRes(
            R.string.feature_shoppinglist_detail_toolbar_title,
            if (shoppingListItem == null) {
                R.string.feature_shoppinglist_add
            } else {
                R.string.feature_shoppinglist_update
            },
        )
        val context = LocalContext.current
        val focusManager = LocalFocusManager.current
        val showProgressBar by remember(detailState, saveState) {
            derivedStateOf {
                detailState is State.Loading || saveState is State.Loading
            }
        }

        val saveStateData by remember(saveState) {
            derivedStateOf {
                (saveState as? State.Success)?.data
            }
        }

        val shouldResetFields by remember(saveStateData, shoppingListItem) {
            derivedStateOf {
                saveStateData != null && shoppingListItem == null
            }
        }

        LaunchedEffect(saveState, shoppingListItem) {
            if (saveState is State.Success) {
                if (saveState.data == null) {
                    return@LaunchedEffect
                }
                if (shoppingListItem == null) {
                    config.shared.snackbarHostState.showSnackbar(
                        duration = SnackbarDuration.Short,
                        message = context.getString(R.string.feature_shoppinglist_detail_success_adding_item),
                    )
                } else {
                    config.onUpdateSuccess.invoke()
                }
            } else if (saveState is State.Error) {
                config.shared.snackbarHostState.showSnackbar(
                    duration = SnackbarDuration.Long,
                    message = saveState.getMessage(context),
                )
            }
        }

        LaunchedEffect(detailState) {
            if (detailState is State.Error) {
                val message = detailState.getMessage(context)
                config.shared.snackbarHostState.showSnackbar(
                    message = message,
                    duration = SnackbarDuration.Long,
                )
            }
        }

        Scaffold(
            topBar = {
                ToolbarWithProgressbar(
                    title = toolbarTitle,
                    showProgress = showProgressBar,
                    onNavigationIconClicked = config.shared.onNavigationIconClicked,
                )
            },
            snackbarHost = {
                SnackbarHost(hostState = config.shared.snackbarHostState)
            },
        ) { values: PaddingValues ->
            Column(
                modifier = modifier
                    .padding(values)
                    .safeContentPadding()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                val groupedNumberFormat = rememberNumberFormat()
                val unGroupedNumberFormat = rememberNumberFormat {
                    isGroupingUsed = false
                }

                var name by rememberSaveable(shoppingListItem?.name,
                    stateSaver = Savers.TEXT_FIELD_VALUE) {
                    mutableStateOf(TextFieldValue(shoppingListItem?.name ?: ""))
                }
                TextInputLayout(
                    modifier = Modifier
                        .fillMaxWidthHorizontalPadding()
                        .padding(top = 16.dp),
                    value = name,
                    onValueChange = { name = it },
                    label = stringResource(R.string.feature_shoppinglist_detail_input_field_label_name),
                    keyboardOptions = DefaultKeyboardOptions.copy(capitalization = KeyboardCapitalization.Words),
                )

                var unit by rememberSaveable(
                    shoppingListItem?.unit,
                    stateSaver = Savers.TEXT_FIELD_VALUE,
                ) {
                    mutableStateOf(TextFieldValue(shoppingListItem?.unit ?: ""))
                }

                var unitQuantity by rememberSaveable(
                    shoppingListItem?.unitQuantity,
                    stateSaver = Savers.TEXT_FIELD_VALUE,
                ) {
                    mutableStateOf(TextFieldValue(shoppingListItem?.unitQuantity?.let(
                        unGroupedNumberFormat::format) ?: ""))
                }

                val helpText by remember(name, unit, unitQuantity) {
                    derivedStateOf {
                        (unitQuantity.text.trim().toFloatOrNull() ?: 1f).let {
                            "${it.let(groupedNumberFormat::format)} ${
                                unit.text.ifBlank {
                                    context.resources.getQuantityString(
                                        R.plurals.feature_shoppinglist_default_measurement_unit,
                                        it.toInt(),
                                    )
                                }
                            } of ${name.text.ifBlank { "-" }}"
                        }.trim()
                    }
                }

                TextInputLayout(
                    modifier = Modifier.fillMaxWidthHorizontalPadding(),
                    value = unit,
                    helpText = helpText,
                    onValueChange = { unit = it },
                    label = stringResource(R.string.feature_shoppinglist_detail_input_field_label_measurement_unit),
                    keyboardOptions = DefaultKeyboardOptions.copy(capitalization = KeyboardCapitalization.None),
                )

                TextInputLayout(
                    modifier = Modifier.fillMaxWidthHorizontalPadding(),
                    helpText = helpText,
                    value = unitQuantity,
                    onValueChange = { unitQuantity = it },
                    label = stringResource(R.string.feature_shoppinglist_detail_input_field_label_measurement_unit_quantity),
                    keyboardOptions = DefaultKeyboardOptions.copy(keyboardType = KeyboardType.Decimal),
                )

                var purchaseQuantity by rememberSaveable(
                    shoppingListItem?.purchaseQuantity,
                    stateSaver = Savers.TEXT_FIELD_VALUE,
                ) {
                    val value =
                        if (shoppingListItem != null && shoppingListItem!!.purchaseQuantity != ShoppingListItem.DEFAULT_INSTANCE.purchaseQuantity) {
                            shoppingListItem!!.purchaseQuantity.let(unGroupedNumberFormat::format)
                        } else {
                            ""
                        }
                    mutableStateOf(TextFieldValue(value))
                }
                TextInputLayout(
                    modifier = Modifier.fillMaxWidthHorizontalPadding(),
                    value = purchaseQuantity,
                    onValueChange = { purchaseQuantity = it },
                    label = stringResource(R.string.feature_shoppinglist_detail_input_field_label_purchase_quantity),
                    keyboardOptions = DefaultKeyboardOptions.copy(keyboardType = KeyboardType.Decimal),
                )

                /*var physicalAddress by rememberSaveable(
                    shoppingListItem?.physicalAddress,
                    stateSaver = Savers.TEXT_FIELD_VALUE,
                ) {
                    mutableStateOf(TextFieldValue(shoppingListItem?.physicalAddress ?: ""))
                }
                TextInputLayout(
                    modifier = Modifier.fillMaxWidthHorizontalPadding(),
                    maxLines = 4,
                    singleLine = false,
                    value = physicalAddress,
                    onValueChange = { physicalAddress = it },
                    label = stringResource(R.string.feature_shoppinglist_detail_input_field_label_physical_address),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    keyboardOptions = DefaultKeyboardOptions.copy(imeAction = ImeAction.Done),
                )*/
                LaunchedEffect(shouldResetFields) {
                    if (shouldResetFields) {
                        unit = TextFieldValue()
                        name = TextFieldValue()
                        unitQuantity = TextFieldValue()
                        purchaseQuantity = TextFieldValue()
//                        physicalAddress = TextFieldValue()
                    }
                }
                val requiredFields = arrayOf(
                    name,
                    unit,
                )
                val enableSubmitButton by remember(showProgressBar, *requiredFields) {
                    derivedStateOf {
                        requiredFields.all { it.text.isNotBlank() } && !showProgressBar
                    }
                }
                Button(
                    enabled = enableSubmitButton,
                    modifier = Modifier.fillMaxWidthHorizontalPadding(),
                    onClick = {
                        focusManager.clearFocus()
                        config.onSubmitDetails.invoke((shoppingListItem
                            ?: ShoppingListItem.DEFAULT_INSTANCE).copy(
                            name = name.text.trim(),
                            unit = unit.text.trim().uppercase(),
                            unitQuantity = unitQuantity.text.trim().ifBlank { "1" }.toFloat(),
                            purchaseQuantity = purchaseQuantity.text.trim().ifBlank { "1" }
                                .toFloat(),
                        ))
                    },
                ) {
                    Text(
                        text = toolbarTitle.uppercase(),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}
