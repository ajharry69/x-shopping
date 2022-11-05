package co.ke.xently.shopping.features.shops.ui.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import co.ke.xently.shopping.features.shop.R
import co.ke.xently.shopping.features.shops.repositories.exceptions.ShopHttpException
import co.ke.xently.shopping.features.shops.ui.MapViewWithLoadingIndicator
import co.ke.xently.shopping.features.ui.*
import co.ke.xently.shopping.features.utils.Shared
import co.ke.xently.shopping.features.utils.State
import co.ke.xently.shopping.libraries.data.source.Coordinate
import co.ke.xently.shopping.libraries.data.source.Shop
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberMarkerState

internal object ShopDetailScreen {
    data class Config(
        val shared: Shared = Shared(),
        val onSubmitDetails: (Shop) -> Unit = {},
        val onUpdateSuccess: () -> Unit = shared.onNavigationIconClicked,
    )

    @Composable
    operator fun invoke(
        modifier: Modifier,
        config: Config,
        id: Long,
        viewModel: ShopDetailViewModel = hiltViewModel(),
    ) {
        val state by viewModel.detailState.collectAsState()
        val saveState by viewModel.saveState.collectAsState(State.Success(null))
        LaunchedEffect(id) {
            viewModel.get(id)
        }
        ShopDetailScreen(
            modifier = modifier,
            state = state,
            saveState = saveState,
            config = config.copy(onSubmitDetails = viewModel::save),
        )
    }

    @Composable
    operator fun invoke(
        modifier: Modifier,
        config: Config,
        state: State<Shop>,
        saveState: State<String>,
    ) {
        val context = LocalContext.current
        val focusManager = LocalFocusManager.current

        val detailScreen = DetailScreen(
            state = state,
            saveState = saveState,
            snackbarHostState = config.shared.snackbarHostState,
            onUpdateSuccess = config.onUpdateSuccess,
            onAddSuccess = {
                config.shared.snackbarHostState.showSnackbar(
                    duration = SnackbarDuration.Short,
                    message = context.getString(R.string.feature_shops_add_success),
                )
            },
        )
        val (shop, shouldResetFields, showProgressIndicator) = detailScreen

        var coordinates by remember(shop) {
            mutableStateOf(shop?.coordinates)
        }

        val appBarTitle = detailScreen.title(R.string.feature_shops_detail_toolbar_title)

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
                MapViewWithLoadingIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(550.dp),
                    onMapClick = {
                        coordinates = Coordinate(it.latitude, it.longitude)
                    },
                ) {
                    coordinates?.also {
                        val markerState = rememberMarkerState(
                            position = LatLng(it.lat, it.lon),
                        )
                        Marker(
                            draggable = true,
                            state = markerState,
                            onClick = {
                                coordinates = null
                                true
                            },
                        )
                    }
                }

                val name = TextFieldConfig(
                    labelId = R.string.feature_shops_detail_input_field_label_name,
                    valueInputs = shop?.name,
                    state = saveState,
                    shouldResetField = shouldResetFields,
                ) {
                    (it.error as? ShopHttpException)?.name?.joinToString("\n")
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

                val taxPin = TextFieldConfig(
                    labelId = R.string.feature_shops_detail_input_field_label_tax_pin,
                    valueInputs = shop?.taxPin,
                    state = saveState,
                    shouldResetField = shouldResetFields,
                ) {
                    (it.error as? ShopHttpException)?.taxPin?.joinToString("\n")
                }

                TextField(
                    modifier = Modifier.fillMaxWidthHorizontalPadding(),
                    value = taxPin.value,
                    label = {
                        Text(taxPin.label)
                    },
                    isError = taxPin.hasError,
                    onValueChange = taxPin.onValueChange,
                    supportingText = {
                        SupportingText(config = taxPin)
                    },
                    keyboardOptions = DefaultKeyboardOptions.copy(
                        imeAction = ImeAction.Done,
                        capitalization = KeyboardCapitalization.Characters,
                    ),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                )

                val requiredFields = arrayOf(name, taxPin)
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
                        config.onSubmitDetails.invoke((shop
                            ?: Shop.DEFAULT_INSTANCE).copy(
                            name = name.value.text.trim(),
                            taxPin = taxPin.value.text.trim(),
                        ).run {
                            coordinates?.let { copy(coordinates = it) } ?: this
                        })
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
