package co.ke.xently.shopping.features.shops.ui.detail

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
import co.ke.xently.shopping.features.shop.R
import co.ke.xently.shopping.features.shops.repositories.exceptions.ShopHttpException
import co.ke.xently.shopping.features.stringRes
import co.ke.xently.shopping.features.ui.*
import co.ke.xently.shopping.features.utils.Shared
import co.ke.xently.shopping.features.utils.State
import co.ke.xently.shopping.libraries.data.source.Shop

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
        val shop by remember(state) {
            derivedStateOf {
                (state as? State.Success)?.data
            }
        }
        val toolbarTitle = stringRes(
            R.string.feature_shops_detail_toolbar_title,
            if (shop == null) {
                R.string.feature_shops_add
            } else {
                R.string.feature_shops_update
            },
        )
        val context = LocalContext.current
        val focusManager = LocalFocusManager.current
        val showProgressBar by remember(state, saveState) {
            derivedStateOf {
                state is State.Loading || saveState is State.Loading
            }
        }

        val saveStateData by remember(saveState) {
            derivedStateOf {
                (saveState as? State.Success)?.data
            }
        }

        val shouldResetFields by remember(saveStateData, shop) {
            derivedStateOf {
                saveStateData != null && shop == null
            }
        }

        LaunchedEffect(saveState, shop) {
            if (saveState is State.Success) {
                if (saveState.data == null) {
                    return@LaunchedEffect
                }
                if (shop == null) {
                    config.shared.snackbarHostState.showSnackbar(
                        duration = SnackbarDuration.Short,
                        message = context.getString(R.string.feature_shops_detail_success_adding_item),
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

        LaunchedEffect(state) {
            if (state is State.Error) {
                val message = state.getMessage(context)
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
                val name = TextFieldConfig(
                    labelId = R.string.feature_shops_detail_input_field_label_name,
                    valueInputs = shop?.name,
                    state = saveState,
                    shouldResetField = shouldResetFields,
                ) {
                    (it.error as? ShopHttpException)?.name?.joinToString("\n")
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
                    keyboardOptions = DefaultKeyboardOptions.copy(keyboardType = KeyboardType.Decimal),
                    supportingText = {
                        SupportingText(config = taxPin)
                    },
                )

                val requiredFields = arrayOf(
                    name,
                    taxPin,
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
                        config.onSubmitDetails.invoke((shop
                            ?: Shop.DEFAULT_INSTANCE).copy(
                            name = name.value.text.trim(),
                            taxPin = taxPin.value.text.trim(),
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