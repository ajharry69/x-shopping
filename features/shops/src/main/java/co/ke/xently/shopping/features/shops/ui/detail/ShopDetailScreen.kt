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
import co.ke.xently.shopping.features.map.MapViewWithLoadingIndicator
import co.ke.xently.shopping.features.shops.R
import co.ke.xently.shopping.features.shops.ShopsNavGraph
import co.ke.xently.shopping.features.shops.repositories.exceptions.ShopHttpException
import co.ke.xently.shopping.features.ui.*
import co.ke.xently.shopping.features.utils.Shared
import co.ke.xently.shopping.features.utils.State
import co.ke.xently.shopping.libraries.data.source.Coordinate
import co.ke.xently.shopping.libraries.data.source.Shop
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberMarkerState
import com.ramcosta.composedestinations.annotation.Destination

data class Args(val name: String? = null, val id: Long = Shop.DEFAULT_INSTANCE.id)

@ShopsNavGraph(start = true)
@Destination(navArgsDelegate = Args::class)
@Composable
internal fun ShopDetailScreen(
    args: Args,
    shared: Shared,
    viewModel: ShopDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.detailState.collectAsState()
    val saveState by viewModel.saveState.collectAsState(State.Success(null))
    LaunchedEffect(args.id) {
        viewModel.get(args.id)
    }
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    val detailScreen = DetailScreen(
        state = state,
        saveState = saveState,
        snackbarHostState = shared.snackbarHostState,
        onUpdateSuccess = shared.onNavigationIconClicked,
        onAddSuccess = {
            shared.snackbarHostState.showSnackbar(
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
                valueInputs = shop?.name ?: args.name,
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
                    (shop ?: Shop.DEFAULT_INSTANCE).copy(
                        name = name.value.text.trim(),
                        taxPin = taxPin.value.text.trim(),
                    ).run {
                        coordinates?.let { copy(coordinates = it) } ?: this
                    }.let(viewModel::save)
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
