package co.ke.xently.shopping.features.shoppinglist.ui.detail

import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import co.ke.xently.shopping.features.TextFieldResource.Companion.compileResource
import co.ke.xently.shopping.features.shoppinglist.R
import co.ke.xently.shopping.features.shoppinglist.repositories.exceptions.ShoppingListItemHttpException
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

                val name = compileResource(
                    labelId = R.string.feature_shoppinglist_detail_input_field_label_name,
                    valueInputs = shoppingListItem?.name,
                    state = saveState,
                    shouldResetField = shouldResetFields,
                ) {
                    (it.error as? ShoppingListItemHttpException)?.name?.joinToString("\n")
                }
                TextInputLayout(
                    modifier = Modifier
                        .fillMaxWidthHorizontalPadding()
                        .padding(top = 16.dp),
                    value = name.value,
                    error = name.error,
                    label = name.label,
                    isError = name.hasError,
                    onValueChange = name.onValueChange,
                    keyboardOptions = DefaultKeyboardOptions.copy(capitalization = KeyboardCapitalization.Words),
                )

                val unit = compileResource(
                    labelId = R.string.feature_shoppinglist_detail_input_field_label_measurement_unit,
                    valueInputs = shoppingListItem?.unit,
                    state = saveState,
                    shouldResetField = shouldResetFields,
                ) {
                    (it.error as? ShoppingListItemHttpException)?.unit?.joinToString("\n")
                }

                val unitQuantity = compileResource(
                    labelId = R.string.feature_shoppinglist_detail_input_field_label_measurement_unit_quantity,
                    valueInputs = shoppingListItem?.unitQuantity,
                    state = saveState,
                    shouldResetField = shouldResetFields,
                    defaultValue = unGroupedNumberFormat::format,
                ) {
                    (it.error as? ShoppingListItemHttpException)?.unitQuantity?.joinToString("\n")
                }

                val helpText by remember(name.value, unit.value, unitQuantity.value) {
                    derivedStateOf {
                        (unitQuantity.value.text.trim().toFloatOrNull()
                            ?: 1f).let {
                            "${it.let(groupedNumberFormat::format)} ${
                                unit.value.text.ifBlank {
                                    context.resources.getQuantityString(
                                        R.plurals.feature_shoppinglist_default_measurement_unit,
                                        it.toInt(),
                                    )
                                }
                            } of ${name.value.text.ifBlank { "-" }}"
                        }.trim()
                    }
                }

                TextInputLayout(
                    modifier = Modifier.fillMaxWidthHorizontalPadding(),
                    helpText = helpText,
                    value = unit.value,
                    error = unit.error,
                    label = unit.label,
                    isError = unit.hasError,
                    onValueChange = unit.onValueChange,
                    keyboardOptions = DefaultKeyboardOptions.copy(capitalization = KeyboardCapitalization.None),
                )

                TextInputLayout(
                    modifier = Modifier.fillMaxWidthHorizontalPadding(),
                    helpText = helpText,
                    value = unitQuantity.value,
                    error = unitQuantity.error,
                    label = unitQuantity.label,
                    isError = unitQuantity.hasError,
                    onValueChange = unitQuantity.onValueChange,
                    keyboardOptions = DefaultKeyboardOptions.copy(keyboardType = KeyboardType.Decimal),
                )

                val purchaseQuantity = compileResource(
                    labelId = R.string.feature_shoppinglist_detail_input_field_label_purchase_quantity,
                    valueInputs = shoppingListItem?.purchaseQuantity,
                    state = saveState,
                    shouldResetField = shouldResetFields,
                    defaultValue = {
                        it.takeIf { quantity ->
                            quantity != ShoppingListItem.DEFAULT_INSTANCE.purchaseQuantity
                        }?.let(unGroupedNumberFormat::format)
                    },
                ) {
                    (it.error as? ShoppingListItemHttpException)?.purchaseQuantity?.joinToString("\n")
                }

                TextInputLayout(
                    modifier = Modifier.fillMaxWidthHorizontalPadding(),
                    value = purchaseQuantity.value,
                    error = purchaseQuantity.error,
                    label = purchaseQuantity.label,
                    isError = purchaseQuantity.hasError,
                    onValueChange = purchaseQuantity.onValueChange,
                    keyboardOptions = DefaultKeyboardOptions.copy(keyboardType = KeyboardType.Decimal),
                )

                val requiredFields = arrayOf(name, unit, unitQuantity, purchaseQuantity)
                val enableSubmitButton by remember(showProgressBar, *requiredFields) {
                    derivedStateOf {
                        requiredFields.all { !it.hasError } && !showProgressBar
                    }
                }
                Button(
                    enabled = enableSubmitButton,
                    modifier = Modifier.fillMaxWidthHorizontalPadding(),
                    onClick = {
                        focusManager.clearFocus()
                        config.onSubmitDetails.invoke((shoppingListItem
                            ?: ShoppingListItem.DEFAULT_INSTANCE).copy(
                            name = name.value.text.trim(),
                            unit = unit.value.text.trim().uppercase(),
                            unitQuantity = unitQuantity.value.text.trim()
                                .ifBlank { "1" }.toFloat(),
                            purchaseQuantity = purchaseQuantity.value.text.trim().ifBlank { "1" }
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
