package co.ke.xently.shopping.features.products.ui.detail

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
import co.ke.xently.shopping.features.products.R
import co.ke.xently.shopping.features.products.repositories.exceptions.ProductHttpException
import co.ke.xently.shopping.features.products.shared.AttributeNameSearchViewModel
import co.ke.xently.shopping.features.products.shared.AttributeSearchViewModel
import co.ke.xently.shopping.features.products.shared.BrandSearchViewModel
import co.ke.xently.shopping.features.products.shared.MeasurementUnitSearchViewModel
import co.ke.xently.shopping.features.products.shared.ui.AttributesInput
import co.ke.xently.shopping.features.products.shared.ui.BrandsInput
import co.ke.xently.shopping.features.products.shopsearch.ShopSearchViewModel
import co.ke.xently.shopping.features.products.ui.ShopInput
import co.ke.xently.shopping.features.ui.*
import co.ke.xently.shopping.features.utils.Query
import co.ke.xently.shopping.features.utils.Shared
import co.ke.xently.shopping.features.utils.State
import co.ke.xently.shopping.libraries.data.source.AbstractAttribute
import co.ke.xently.shopping.libraries.data.source.AbstractBrand
import co.ke.xently.shopping.libraries.data.source.Product
import co.ke.xently.shopping.libraries.data.source.Shop

internal object ProductDetailScreen {
    data class Config(
        val shared: Shared = Shared(),
        val onSubmitDetails: (Product) -> Unit = {},
        val onAddNewShopClicked: (String) -> Unit = {},
        val onUpdateSuccess: () -> Unit = shared.onNavigationIconClicked,
    )

    @Composable
    operator fun invoke(
        id: Long,
        modifier: Modifier,
        config: Config,
        viewModel: ProductDetailViewModel = hiltViewModel(),
        shopSearchViewModel: ShopSearchViewModel = hiltViewModel(),
        brandSearchViewModel: BrandSearchViewModel = hiltViewModel(),
        attributeSearchViewModel: AttributeSearchViewModel = hiltViewModel(),
        attributeNameSearchViewModel: AttributeNameSearchViewModel = hiltViewModel(),
        measurementUnitSearchViewModel: MeasurementUnitSearchViewModel = hiltViewModel(),
    ) {
        val detailState by viewModel.detailState.collectAsState()
        val shopSuggestions by shopSearchViewModel.searchAutoCompleteResults.collectAsState()
        val brandSuggestions by brandSearchViewModel.searchAutoCompleteResults.collectAsState()
        val attributeSuggestions by attributeSearchViewModel.searchAutoCompleteResults.collectAsState()
        val attributeNameSuggestions by attributeNameSearchViewModel.searchAutoCompleteResults.collectAsState()
        val measurementUnitSuggestions by measurementUnitSearchViewModel.searchAutoCompleteResults.collectAsState()
        val saveState by viewModel.saveState.collectAsState(State.Success(null))
        LaunchedEffect(id) {
            viewModel.get(id)
        }
        ProductDetailScreen(
            modifier = modifier,
            saveState = saveState,
            detailState = detailState,
            config = config.copy(onSubmitDetails = viewModel::save),
            shopSuggestions = shopSuggestions,
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
            onShopQueryChange = shopSearchViewModel::autoCompleteSearch,
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
        detailState: State<Product>,
        shopSuggestions: List<Shop>,
        brandSuggestions: List<AbstractBrand>,
        attributeSuggestions: List<AbstractAttribute>,
        attributeNameSuggestions: List<String>,
        measurementUnitSuggestions: List<String>,
        onShopQueryChange: (String) -> Unit = {},
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
                    message = context.getString(R.string.feature_products_add_success),
                )
            },
        )
        val (product, shouldResetFields, showProgressIndicator) = detailScreen

        val appBarTitle = detailScreen.title(R.string.feature_products_detail_toolbar_title)

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

                val (shopConfig, selectedShop) = ShopInput(
                    modifier = Modifier.fillMaxWidthHorizontalPadding(),
                    state = saveState,
                    shouldResetFields = shouldResetFields,
                    default = product?.shop?.takeIf { it.id != Shop.DEFAULT_INSTANCE.id },
                    suggestions = shopSuggestions,
                    onQueryChange = onShopQueryChange,
                    errorMessage = {
                        (it.error as? ProductHttpException)?.shop?.joinToString("\n")
                    },
                ) {
                    config.onAddNewShopClicked(it.value.text.trim())
                }

                val name = TextFieldConfig(
                    labelId = R.string.feature_products_detail_input_field_label_name,
                    valueInputs = product?.name,
                    state = saveState,
                    shouldResetField = shouldResetFields,
                ) {
                    (it.error as? ProductHttpException)?.name?.joinToString("\n")
                }
                TextField(
                    modifier = Modifier.fillMaxWidthHorizontalPadding(),
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
                    labelId = R.string.feature_products_detail_input_field_label_measurement_unit,
                    valueInputs = product?.unit,
                    state = saveState,
                    shouldResetField = shouldResetFields,
                ) {
                    (it.error as? ProductHttpException)?.unit?.joinToString("\n")
                }

                val unitQuantity = TextFieldConfig(
                    labelId = R.string.feature_products_detail_input_field_label_measurement_unit_quantity,
                    valueInputs = product?.unitQuantity,
                    state = saveState,
                    shouldResetField = shouldResetFields,
                    defaultValue = unGroupedNumberFormat::format,
                ) {
                    (it.error as? ProductHttpException)?.unitQuantity?.joinToString("\n")
                }

                val helpText by remember(name.value, unit.value, unitQuantity.value) {
                    derivedStateOf {
                        unitQuantity.value.text.trim().toFloatOrNull()?.let {
                            "${it.let(groupedNumberFormat::format)} ${
                                unit.value.text.ifBlank {
                                    context.resources.getQuantityString(
                                        R.plurals.feature_products_default_measurement_unit,
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

                val unitPrice = TextFieldConfig(
                    labelId = R.string.feature_products_detail_input_field_label_unit_price,
                    valueInputs = product?.unitPrice,
                    state = saveState,
                    shouldResetField = shouldResetFields,
                    defaultValue = {
                        it.takeIf { price ->
                            price != Product.DEFAULT_INSTANCE.unitPrice
                        }?.let(unGroupedNumberFormat::format)
                    },
                ) {
                    (it.error as? ProductHttpException)?.unitPrice?.joinToString("\n")
                }

                TextField(
                    modifier = Modifier.fillMaxWidthHorizontalPadding(),
                    value = unitPrice.value,
                    label = { Text(unitPrice.label) },
                    isError = unitPrice.hasError,
                    onValueChange = unitPrice.onValueChange,
                    keyboardOptions = DefaultKeyboardOptions.copy(keyboardType = KeyboardType.Decimal),
                    supportingText = {
                        SupportingText(config = unitPrice)
                    },
                )

                val (brandConfig, selectedBrands) = BrandsInput(
                    state = saveState,
                    shouldResetFields = shouldResetFields,
                    default = product?.brands,
                    suggestions = brandSuggestions,
                    onQueryChange = onBrandQueryChange,
                    create = {
                        Product.Brand(it.name)
                    },
                    errorMessage = {
                        (it.error as? ProductHttpException)?.brands?.joinToString("\n")
                    },
                )

                val (attributeNameConfig, attributeValueConfig, selectedAttributes) = AttributesInput(
                    snackbarState = config.shared.snackbarHostState,
                    state = saveState,
                    shouldResetFields = shouldResetFields,
                    default = product?.attributes,
                    suggestions = attributeSuggestions,
                    nameSuggestions = attributeNameSuggestions,
                    onAttributeNameQueryChange = onAttributeNameQueryChange,
                    onAttributeValueQueryChange = onAttributeValueQueryChange,
                    create = {
                        Product.Attribute(it.name, it.value, emptyList())
                    },
                    errorMessage = {
                        (it.error as? ProductHttpException)?.attributes?.joinToString("\n")
                    },
                )

                val requiredFields = arrayOf(
                    name,
                    unit,
                    shopConfig,
                    brandConfig,
                    attributeNameConfig,
                    attributeValueConfig,
                    unitQuantity,
                    unitPrice,
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
                        config.onSubmitDetails.invoke((product
                            ?: Product.DEFAULT_INSTANCE).copy(
                            shop = selectedShop,
                            brands = selectedBrands.map {
                                if (it is Product.Brand) {
                                    it
                                } else {
                                    Product.Brand(name = it.name)
                                }
                            },
                            attributes = selectedAttributes.map {
                                if (it is Product.Attribute) {
                                    it
                                } else {
                                    Product.Attribute(name = it.name, value = it.value)
                                }
                            },
                            name = name.value.text.trim(),
                            unit = unit.value.text.trim(),
                            unitQuantity = unitQuantity.value.text.trim().ifBlank { "1" }.toFloat(),
                            unitPrice = unitPrice.value.text.trim().ifBlank { "1" }
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
