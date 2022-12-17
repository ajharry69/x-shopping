package co.ke.xently.shopping.features.products.ui.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.PagingConfig
import androidx.paging.compose.collectAsLazyPagingItems
import co.ke.xently.shopping.features.products.ProductsNavGraph
import co.ke.xently.shopping.features.products.ProductsNavigator
import co.ke.xently.shopping.features.products.R
import co.ke.xently.shopping.features.products.models.Product
import co.ke.xently.shopping.features.products.repositories.exceptions.ProductHttpException
import co.ke.xently.shopping.features.products.shared.AttributeNameSearchViewModel
import co.ke.xently.shopping.features.products.shared.AttributeSearchViewModel
import co.ke.xently.shopping.features.products.shared.BrandSearchViewModel
import co.ke.xently.shopping.features.products.shared.MeasurementUnitSearchViewModel
import co.ke.xently.shopping.features.products.shared.ui.AttributesInput
import co.ke.xently.shopping.features.products.shared.ui.BrandsInput
import co.ke.xently.shopping.features.products.ui.ShopInput
import co.ke.xently.shopping.features.search.PaginatedSearchViewModel
import co.ke.xently.shopping.features.shops.search.ShopPaginatedSearchViewModel
import co.ke.xently.shopping.features.ui.*
import co.ke.xently.shopping.features.utils.Query
import co.ke.xently.shopping.features.utils.Shared
import co.ke.xently.shopping.features.utils.State
import co.ke.xently.shopping.libraries.data.source.Shop
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.ramcosta.composedestinations.annotation.Destination
import java.text.SimpleDateFormat
import java.util.*

@ProductsNavGraph(start = true)
@Destination
@Composable
internal fun ProductDetailScreen(
    shared: Shared,
    navigator: ProductsNavigator,
    id: Long = Product.DEFAULT_INSTANCE.id,
    viewModel: ProductDetailViewModel = hiltViewModel(),
    shopPaginatedSearchViewModel: ShopPaginatedSearchViewModel = hiltViewModel(),
    brandSearchViewModel: BrandSearchViewModel = hiltViewModel(),
    attributeSearchViewModel: AttributeSearchViewModel = hiltViewModel(),
    attributeNameSearchViewModel: AttributeNameSearchViewModel = hiltViewModel(),
    measurementUnitSearchViewModel: MeasurementUnitSearchViewModel = hiltViewModel(),
) {
    val detailState by viewModel.detailState.collectAsState()
    val shopSuggestions = shopPaginatedSearchViewModel.searchAutoComplete.collectAsLazyPagingItems()
    val brandSuggestions by brandSearchViewModel.searchAutoCompleteResults.collectAsState()
    val attributeSuggestions by attributeSearchViewModel.searchAutoCompleteResults.collectAsState()
    val attributeNameSuggestions by attributeNameSearchViewModel.searchAutoCompleteResults.collectAsState()
    val measurementUnitSuggestions by measurementUnitSearchViewModel.searchAutoCompleteResults.collectAsState()
    val saveState by viewModel.saveState.collectAsState(State.Success(null))

    LaunchedEffect(id) {
        viewModel.get(id)
    }

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val fragmentManager = rememberFragmentManager()
    val dateFormat = SimpleDateFormat.getDateInstance()

    val detailScreen = DetailScreen(
        state = detailState,
        saveState = saveState,
        snackbarHostState = shared.snackbarHostState,
        onUpdateSuccess = shared.onNavigationIconClicked,
        onAddSuccess = {
            shared.snackbarHostState.showSnackbar(
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
                        MoveBackNavigationIconButton(shared)
                    },
                )
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = shared.snackbarHostState)
        },
    ) { values: PaddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
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
                onQueryChange = {
                    val request = PaginatedSearchViewModel.Request(
                        query = Query(it),
                        config = PagingConfig(pageSize = 10, enablePlaceholders = false),
                    )
                    shopPaginatedSearchViewModel.autoCompleteSearch(request)
                },
                errorMessage = {
                    (it.error as? ProductHttpException)?.shop?.joinToString("\n")
                },
            ) {
                navigator.onAddNewShopClicked(it.value.text.trim())
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
                onQueryChange = measurementUnitSearchViewModel::autoCompleteSearch,
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

            val purchasedQuantity = TextFieldConfig(
                labelId = R.string.feature_products_detail_input_field_label_purchased_quantity,
                valueInputs = product?.purchasedQuantity,
                state = saveState,
                shouldResetField = shouldResetFields,
                defaultValue = unGroupedNumberFormat::format,
            ) {
                (it.error as? ProductHttpException)?.purchasedQuantity?.joinToString("\n")
            }

            TextField(
                modifier = Modifier.fillMaxWidthHorizontalPadding(),
                value = purchasedQuantity.value,
                label = {
                    Text(purchasedQuantity.label)
                },
                isError = purchasedQuantity.hasError,
                onValueChange = purchasedQuantity.onValueChange,
                keyboardOptions = DefaultKeyboardOptions.copy(keyboardType = KeyboardType.Decimal),
            )

            var datePurchased by rememberSaveable(
                product?.datePurchased,
                stateSaver = Savers.DATE,
            ) {
                mutableStateOf(product?.datePurchased ?: Date())
            }
            val datePurchasedPicker = Dialog.rememberDatePicker(
                select = datePurchased,
                title = R.string.feature_products_detail_input_field_label_date_of_purchase,
                bounds = CalendarConstraints.Builder()
                    .setValidator(DateValidatorPointBackward.before(Date().time)).build(),
            ) {
                datePurchased = it
            }

            val dateOfPurchase = TextFieldConfig(
                labelId = R.string.feature_products_detail_input_field_label_date_of_purchase,
                valueInputs = datePurchased,
                state = saveState,
                shouldResetField = shouldResetFields,
                defaultValue = dateFormat::format,
            ) {
                (it.error as? ProductHttpException)?.datePurchased?.joinToString("\n")
            }

            TextField(
                modifier = Modifier.fillMaxWidthHorizontalPadding(),
                value = dateOfPurchase.value,
                label = {
                    Text(dateOfPurchase.label)
                },
                isError = dateOfPurchase.hasError,
                onValueChange = dateOfPurchase.onValueChange,
                keyboardOptions = DefaultKeyboardOptions,
                trailingIcon = {
                    IconButton(onClick = {
                        focusManager.clearFocus()
                        datePurchasedPicker.show(fragmentManager, "TAG_DATE_OF_PURCHASE_PICKER")
                    }) {
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = stringResource(R.string.feature_products_content_desc_pick_date_of_purchase),
                        )
                    }
                },
            )

            val (brandConfig, selectedBrands) = BrandsInput(
                state = saveState,
                shouldResetFields = shouldResetFields,
                default = product?.brands,
                suggestions = brandSuggestions,
                onQueryChange = {
                    val query = Query(
                        value = it,
                        filters = mapOf("uniqueByName" to true),
                    )
                    brandSearchViewModel.autoCompleteSearch(query)
                },
                create = {
                    Product.Brand(it.name)
                },
                errorMessage = {
                    (it.error as? ProductHttpException)?.brands?.joinToString("\n")
                },
            )

            val (attributeNameConfig, attributeValueConfig, selectedAttributes) = AttributesInput(
                snackbarState = shared.snackbarHostState,
                state = saveState,
                shouldResetFields = shouldResetFields,
                default = product?.attributes,
                suggestions = attributeSuggestions,
                nameSuggestions = attributeNameSuggestions,
                onAttributeNameQueryChange = attributeNameSearchViewModel::autoCompleteSearch,
                onAttributeValueQueryChange = { _name, value ->
                    val query = Query(
                        value = value,
                        filters = mapOf("name" to _name),
                    )
                    attributeSearchViewModel.autoCompleteSearch(query)
                },
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
                purchasedQuantity,
                dateOfPurchase,
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
                    (product ?: Product.DEFAULT_INSTANCE).copy(
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
                        purchasedQuantity = purchasedQuantity.value.text.trim().ifBlank { "1" }
                            .toFloat(),
                        unitPrice = unitPrice.value.text.trim().ifBlank { "1" }
                            .toFloat(),
                        datePurchased = datePurchased!!,
                    ).let(viewModel::save)
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
