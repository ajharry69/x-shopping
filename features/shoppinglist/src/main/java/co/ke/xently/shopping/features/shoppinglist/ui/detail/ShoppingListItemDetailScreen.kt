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
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import co.ke.xently.shopping.features.shoppinglist.R
import co.ke.xently.shopping.features.shoppinglist.repositories.exceptions.ShoppingListItemHttpException
import co.ke.xently.shopping.features.shoppinglist.ui.shared.*
import co.ke.xently.shopping.features.ui.*
import co.ke.xently.shopping.features.utils.Query
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
        brandSearchViewModel: BrandSearchViewModel = hiltViewModel(),
        attributeSearchViewModel: AttributeSearchViewModel = hiltViewModel(),
        attributeNameSearchViewModel: AttributeNameSearchViewModel = hiltViewModel(),
        measurementUnitSearchViewModel: MeasurementUnitSearchViewModel = hiltViewModel(),
    ) {
        val detailState by viewModel.detailState.collectAsState()
        val brandSuggestions by brandSearchViewModel.searchAutoCompleteResults.collectAsState()
        val attributeSuggestions by attributeSearchViewModel.searchAutoCompleteResults.collectAsState()
        val attributeNameSuggestions by attributeNameSearchViewModel.searchAutoCompleteResults.collectAsState()
        val measurementUnitSuggestions by measurementUnitSearchViewModel.searchAutoCompleteResults.collectAsState()
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
            attributeNameSuggestions = attributeNameSuggestions,
            measurementUnitSuggestions = measurementUnitSuggestions,
            onBrandQueryChange = {
                val query = Query(
                    value = it,
                    filters = mapOf("uniqueByName" to true),
                )
                brandSearchViewModel.autoCompleteSearch(query)
            },
            onAttributeValueQueryChange = { name, value ->
                val query = Query(
                    value = value,
                    filters = mapOf("name" to name),
                )
                attributeSearchViewModel.autoCompleteSearch(query)
            },
            onAttributeNameQueryChange = attributeNameSearchViewModel::autoCompleteSearch,
            onMeasurementUnitQueryChange = measurementUnitSearchViewModel::autoCompleteSearch,
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
        attributeNameSuggestions: List<String>,
        measurementUnitSuggestions: List<String>,
        onBrandQueryChange: (String) -> Unit = {},
        onAttributeNameQueryChange: (String) -> Unit = {},
        onAttributeValueQueryChange: (String, String) -> Unit = { _, _ -> },
        onMeasurementUnitQueryChange: (String) -> Unit = {},
    ) {
        val context = LocalContext.current
        val focusManager = LocalFocusManager.current

        val detailScreen = DetailScreen(
            state = detailState,
            saveState = saveState,
            snackbarHostState = config.shared.snackbarHostState,
            onUpdateSuccess = config.onUpdateSuccess,
            onAddSuccess = {
                config.shared.snackbarHostState.showSnackbar(
                    duration = SnackbarDuration.Short,
                    message = context.getString(R.string.feature_shoppinglist_add_success),
                )
            },
        )
        val (item, shouldResetFields, showProgressIndicator) = detailScreen

        val appBarTitle = detailScreen.title(R.string.feature_shoppinglist_detail_toolbar_title)

        Scaffold(
            topBar = {
                TopAppBarWithProgressIndicator(showProgressIndicator = showProgressIndicator) {
                    TopAppBar(
                        title = {
                            Text(appBarTitle)
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
                    valueInputs = item?.name,
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
                    valueInputs = item?.unit,
                    state = saveState,
                    shouldResetField = shouldResetFields,
                ) {
                    (it.error as? ShoppingListItemHttpException)?.unit?.joinToString("\n")
                }

                val unitQuantity = TextFieldConfig(
                    labelId = R.string.feature_shoppinglist_detail_input_field_label_measurement_unit_quantity,
                    valueInputs = item?.unitQuantity,
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
                    onQueryChange = onMeasurementUnitQueryChange,
                )

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
                    valueInputs = item?.purchaseQuantity,
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
                    default = item?.brands,
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
                    default = item?.attributes,
                    suggestions = attributeSuggestions,
                    nameSuggestions = attributeNameSuggestions,
                    onAttributeNameQueryChange = onAttributeNameQueryChange,
                    onAttributeValueQueryChange = onAttributeValueQueryChange,
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
                val enableSubmitButton by remember(showProgressIndicator, *requiredFields) {
                    derivedStateOf {
                        requiredFields.all { !it.hasError } && !showProgressIndicator
                    }
                }
                Button(
                    enabled = enableSubmitButton,
                    modifier = Modifier.fillMaxWidthHorizontalPadding(),
                    onClick = {
                        focusManager.clearFocus()
                        config.onSubmitDetails.invoke((item
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
                        text = appBarTitle.toUpperCase(Locale.current),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}
