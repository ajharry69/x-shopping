package co.ke.xently.shopping.features.shoppinglist.ui.detail

import android.icu.util.MeasureUnit
import android.os.Build
import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import co.ke.xently.shopping.features.shoppinglist.R
import co.ke.xently.shopping.features.shoppinglist.repositories.exceptions.ShoppingListItemHttpException
import co.ke.xently.shopping.features.shoppinglist.ui.AttributesInput
import co.ke.xently.shopping.features.shoppinglist.ui.BrandsInput
import co.ke.xently.shopping.features.stringRes
import co.ke.xently.shopping.features.ui.*
import co.ke.xently.shopping.features.utils.Shared
import co.ke.xently.shopping.features.utils.State
import co.ke.xently.shopping.libraries.data.source.AbstractAttribute
import co.ke.xently.shopping.libraries.data.source.AbstractBrand
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
        val detailState by viewModel.detailState.collectAsState()
        val brandSuggestions by viewModel.brandSuggestions.collectAsState()
        val attributeSuggestions by viewModel.attributeSuggestions.collectAsState()
        val measurementUnitSuggestions by viewModel.measurementUnitSuggestions.collectAsState()
        val saveState by viewModel.saveState.collectAsState(State.Success(null))
        LaunchedEffect(id) {
            viewModel.get(id)
        }
        ShoppingListItemDetailScreen(
            modifier = modifier,
            saveState = saveState,
            detailState = detailState,
            config = config.copy(onSubmitDetails = viewModel::save),
            brandSuggestions = brandSuggestions,
            attributeSuggestions = attributeSuggestions,
            onBrandQueryChange = viewModel::setBrandQuery,
            onAttributeQueryChange = viewModel::setAttributeQuery,
            measurementUnitSuggestions = measurementUnitSuggestions,
            onMeasurementUnitQueryChange = viewModel::setMeasurementUnitQuery,
        )
    }


    @Composable
    @VisibleForTesting
    operator fun invoke(
        config: Config,
        modifier: Modifier,
        saveState: State<String>,
        detailState: State<ShoppingListItem>,
        brandSuggestions: List<AbstractBrand>,
        attributeSuggestions: List<AbstractAttribute>,
        measurementUnitSuggestions: List<MeasureUnit>,
        onBrandQueryChange: (String) -> Unit = {},
        onAttributeQueryChange: (String) -> Unit = {},
        onMeasurementUnitQueryChange: (String) -> Unit = {},
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

                val name = TextFieldConfig(
                    labelId = R.string.feature_shoppinglist_detail_input_field_label_name,
                    valueInputs = shoppingListItem?.name,
                    state = saveState,
                    shouldResetField = shouldResetFields,
                ) {
                    (it.error as? ShoppingListItemHttpException)?.name?.joinToString("\n")
                }
                TextField(
                    modifier = Modifier
                        .fillMaxWidthHorizontalPadding()
                        .padding(top = 16.dp),
                    value = name.value,
                    isError = name.hasError,
                    onValueChange = name.onValueChange,
                    label = {
                        Text(name.label)
                    },
                    supportingText = {
                        SupportingText(config = name)
                    },
                    keyboardOptions = DefaultKeyboardOptions.copy(capitalization = KeyboardCapitalization.Words),
                )

                val unit = TextFieldConfig(
                    labelId = R.string.feature_shoppinglist_detail_input_field_label_measurement_unit,
                    valueInputs = shoppingListItem?.unit,
                    state = saveState,
                    shouldResetField = shouldResetFields,
                ) {
                    (it.error as? ShoppingListItemHttpException)?.unit?.joinToString("\n")
                }

                val unitQuantity = TextFieldConfig(
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
                        unitQuantity.value.text.trim().toFloatOrNull()?.let {
                            "${it.let(groupedNumberFormat::format)} ${
                                unit.value.text.ifBlank {
                                    context.resources.getQuantityString(
                                        R.plurals.feature_shoppinglist_default_measurement_unit,
                                        it.toInt(),
                                    )
                                }
                            } of ${name.value.text.ifBlank { "-" }}"
                        }?.trim()
                    }
                }

                AutoCompleteTextView(
                    modifier = Modifier.fillMaxWidthHorizontalPadding(),
                    suggestions = measurementUnitSuggestions,
                    config = unit,
                    helpText = helpText,
                    onSuggestionSelected = {
                        val text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            it.subtype
                        } else {
                            it.toString()
                        }
                        unit.onValueChange(TextFieldValue(text, selection = TextRange(text.length)))
                    },
                    onQueryChange = onMeasurementUnitQueryChange,
                ) {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                        Text(it.toString())
                    } else {
                        ListItem(
                            headlineText = {
                                Text(it.type)
                            },
                            supportingText = {
                                Text(it.subtype)
                            },
                        )
                    }
                }

                TextField(
                    modifier = Modifier.fillMaxWidthHorizontalPadding(),
                    value = unitQuantity.value,
                    label = {
                        Text(unitQuantity.label)
                    },
                    isError = unitQuantity.hasError,
                    onValueChange = unitQuantity.onValueChange,
                    keyboardOptions = DefaultKeyboardOptions.copy(keyboardType = KeyboardType.Decimal),
                    supportingText = {
                        SupportingText(config = unitQuantity, helpText = helpText)
                    },
                )

                val purchaseQuantity = TextFieldConfig(
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

                TextField(
                    modifier = Modifier.fillMaxWidthHorizontalPadding(),
                    value = purchaseQuantity.value,
                    label = { Text(purchaseQuantity.label) },
                    isError = purchaseQuantity.hasError,
                    onValueChange = purchaseQuantity.onValueChange,
                    keyboardOptions = DefaultKeyboardOptions.copy(keyboardType = KeyboardType.Decimal),
                    supportingText = {
                        SupportingText(config = purchaseQuantity)
                    },
                )

                val (brandConfig, selectedBrands) = BrandsInput(
                    state = saveState,
                    shouldResetFields = shouldResetFields,
                    default = shoppingListItem?.brands,
                    suggestions = brandSuggestions,
                    onQueryChange = onBrandQueryChange,
                    create = {
                        ShoppingListItem.Brand(it.name)
                    },
                    errorMessage = {
                        (it.error as? ShoppingListItemHttpException)?.brands?.joinToString("\n")
                    },
                )

                val (attributeNameConfig, attributeValueConfig, selectedAttributes) = AttributesInput(
                    snackbarState = config.shared.snackbarHostState,
                    state = saveState,
                    shouldResetFields = shouldResetFields,
                    default = shoppingListItem?.attributes,
                    suggestions = attributeSuggestions,
                    onQueryChange = onAttributeQueryChange,
                    create = {
                        ShoppingListItem.Attribute(it.name, it.value, emptyList())
                    },
                    errorMessage = {
                        (it.error as? ShoppingListItemHttpException)?.attributes?.joinToString("\n")
                    },
                )

                val requiredFields = arrayOf(
                    name,
                    unit,
                    brandConfig,
                    attributeNameConfig,
                    attributeValueConfig,
                    unitQuantity,
                    purchaseQuantity,
                )
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
                            brands = selectedBrands.map {
                                if (it is ShoppingListItem.Brand) {
                                    it
                                } else {
                                    ShoppingListItem.Brand(name = it.name)
                                }
                            },
                            attributes = selectedAttributes.map {
                                if (it is ShoppingListItem.Attribute) {
                                    it
                                } else {
                                    ShoppingListItem.Attribute(name = it.name, value = it.value)
                                }
                            },
                            name = name.value.text.trim(),
                            unit = unit.value.text.trim(),
                            unitQuantity = unitQuantity.value.text.trim().ifBlank { "1" }.toFloat(),
                            purchaseQuantity = purchaseQuantity.value.text.trim().ifBlank { "1" }
                                .toFloat(),
                        ))
                    },
                ) {
                    Text(
                        text = toolbarTitle.toUpperCase(Locale.current),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}
