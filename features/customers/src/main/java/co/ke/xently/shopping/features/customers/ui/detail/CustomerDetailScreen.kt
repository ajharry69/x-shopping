package co.ke.xently.shopping.features.customers.ui.detail

import android.telephony.PhoneNumberUtils
import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import co.ke.xently.shopping.features.customers.R
import co.ke.xently.shopping.features.ui.*
import co.ke.xently.shopping.features.utils.Shared
import co.ke.xently.shopping.features.utils.State
import co.ke.xently.shopping.libraries.data.source.Customer

internal object CustomerDetailScreen {
    data class Config(
        val shared: Shared = Shared(),
        val onSubmitDetails: (Customer) -> Unit = {},
        val onUpdateSuccess: () -> Unit = shared.onNavigationIconClicked,
    )

    @Composable
    operator fun invoke(
        id: Int,
        modifier: Modifier,
        config: Config,
        viewModel: CustomerDetailScreenViewModel = hiltViewModel(),
    ) {
        val detailState: State<Customer> by viewModel.detailState.collectAsState()
        val saveState by viewModel.saveState.collectAsState(State.Success(null))
        LaunchedEffect(id) {
            viewModel.get(id)
        }
        CustomerDetailScreen(
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
        detailState: State<Customer>,
    ) {
        val customer by remember(detailState) {
            derivedStateOf {
                (detailState as? State.Success)?.data
            }
        }
        val toolbarTitle = stringRes(
            R.string.fc_detail_toolbar_title,
            if (customer == null) {
                R.string.fc_add
            } else {
                R.string.fc_update
            },
        )
        val context = LocalContext.current
        val focusManager = LocalFocusManager.current
        val showProgressBar by remember(detailState, saveState) {
            derivedStateOf {
                detailState is State.Loading
                        || saveState is State.Loading
            }
        }

        val saveStateData by remember(saveState) {
            derivedStateOf {
                (saveState as? State.Success)?.data
            }
        }

        val shouldResetFields by remember(saveStateData, customer) {
            derivedStateOf {
                saveStateData != null && customer == null
            }
        }

        LaunchedEffect(saveState, customer) {
            if (saveState is State.Success) {
                if (saveState.data == null) {
                    return@LaunchedEffect
                }
                if (customer == null) {
                    config.shared.snackbarHostState.showSnackbar(
                        duration = SnackbarDuration.Short,
                        message = context.getString(R.string.fc_detail_success_adding_customer),
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
                val message = (detailState as? State.Error)?.getMessage(context)
                    ?: context.getString(co.ke.xently.shopping.features.R.string.error_message_generic)
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
                var name by rememberSaveable(customer?.name, stateSaver = Savers.TEXT_FIELD_VALUE) {
                    mutableStateOf(TextFieldValue(customer?.name ?: ""))
                }
                TextInputLayout(
                    modifier = Modifier
                        .fillMaxWidthHorizontalPadding()
                        .padding(top = 16.dp),
                    value = name,
                    onValueChange = { name = it },
                    label = stringResource(R.string.fc_detail_input_field_label_name),
                    keyboardOptions = DefaultKeyboardOptions.copy(capitalization = KeyboardCapitalization.Words),
                )

                var taxPin by rememberSaveable(customer?.taxPin,
                    stateSaver = Savers.TEXT_FIELD_VALUE) {
                    mutableStateOf(TextFieldValue(customer?.taxPin ?: ""))
                }
                TextInputLayout(
                    modifier = Modifier.fillMaxWidthHorizontalPadding(),
                    value = taxPin,
                    onValueChange = { taxPin = it },
                    label = stringResource(R.string.fc_detail_input_field_label_tax_pin),
                    keyboardOptions = DefaultKeyboardOptions.copy(capitalization = KeyboardCapitalization.Characters),
                )

                var phoneNumber by rememberSaveable(
                    customer?.phoneNumber,
                    stateSaver = Savers.TEXT_FIELD_VALUE,
                ) {
                    mutableStateOf(TextFieldValue(customer?.phoneNumber ?: ""))
                }
                TextInputLayout(
                    modifier = Modifier.fillMaxWidthHorizontalPadding(),
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = stringResource(R.string.fc_detail_input_field_label_phone_number),
                    keyboardOptions = DefaultKeyboardOptions.copy(keyboardType = KeyboardType.Phone),
                )

                var exemptionNumber by rememberSaveable(
                    customer?.exemptionNumber,
                    stateSaver = Savers.TEXT_FIELD_VALUE,
                ) {
                    val value =
                        if (customer != null && customer!!.exemptionNumber != Customer.DEFAULT_INSTANCE.exemptionNumber) {
                            customer!!.exemptionNumber
                        } else {
                            ""
                        }
                    mutableStateOf(TextFieldValue(value))
                }
                TextInputLayout(
                    modifier = Modifier.fillMaxWidthHorizontalPadding(),
                    value = exemptionNumber,
                    onValueChange = { exemptionNumber = it },
                    label = stringResource(R.string.fc_detail_input_field_label_exemption_number),
                )

                var physicalAddress by rememberSaveable(
                    customer?.physicalAddress,
                    stateSaver = Savers.TEXT_FIELD_VALUE,
                ) {
                    mutableStateOf(TextFieldValue(customer?.physicalAddress ?: ""))
                }
                TextInputLayout(
                    modifier = Modifier.fillMaxWidthHorizontalPadding(),
                    maxLines = 4,
                    singleLine = false,
                    value = physicalAddress,
                    onValueChange = { physicalAddress = it },
                    label = stringResource(R.string.fc_detail_input_field_label_physical_address),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    keyboardOptions = DefaultKeyboardOptions.copy(imeAction = ImeAction.Done),
                )
                LaunchedEffect(shouldResetFields) {
                    if (shouldResetFields) {
                        taxPin = TextFieldValue()
                        name = TextFieldValue()
                        phoneNumber = TextFieldValue()
                        exemptionNumber = TextFieldValue()
                        physicalAddress = TextFieldValue()
                    }
                }
                val requiredFields = arrayOf(
                    name,
                    taxPin,
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
                        config.onSubmitDetails.invoke(
                            (customer ?: Customer.DEFAULT_INSTANCE).copy(
                                name = name.text.trim(),
                                taxPin = taxPin.text.trim().uppercase(),
                                phoneNumber = PhoneNumberUtils.formatNumber(
                                    phoneNumber.text.trim(),
                                    Locale.current.region,
                                ),
                                exemptionNumber = exemptionNumber.text.trim(),
                                physicalAddress = physicalAddress.text.trim(),
                            )
                        )
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
