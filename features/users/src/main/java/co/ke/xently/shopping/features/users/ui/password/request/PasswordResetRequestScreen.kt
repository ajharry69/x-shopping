package co.ke.xently.shopping.features.users.ui.password.request

import android.util.Patterns
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
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import co.ke.xently.shopping.features.ui.*
import co.ke.xently.shopping.features.ui.TextInputLayout.DefaultKeyboardOptions
import co.ke.xently.shopping.features.users.R
import co.ke.xently.shopping.features.users.repositories.exceptions.PasswordResetRequestHttpException
import co.ke.xently.shopping.features.utils.Shared
import co.ke.xently.shopping.features.utils.State
import co.ke.xently.shopping.libraries.data.source.User

internal object PasswordResetRequestScreen {
    data class Config(
        val shared: Shared = Shared(),
        val onPasswordResetRequest: (String) -> Unit = {},
        val onPasswordResetRequestSuccess: (User) -> Unit = {},
        val onResendPasswordResetRequestCodeClicked: () -> Unit = {},
    )

    @Composable
    operator fun invoke(
        modifier: Modifier,
        config: Config,
        viewModel: PasswordResetRequestScreenViewModel = hiltViewModel(),
    ) {
        val passwordResetRequestState by viewModel.passwordResetRequestState.collectAsState(
            State.Success(null))
        PasswordResetRequestScreen(
            modifier = modifier,
            passwordResetRequestState = passwordResetRequestState,
            config = config.copy(
                onPasswordResetRequest = viewModel::invoke,
            ),
        )
    }

    @Composable
    @VisibleForTesting
    operator fun invoke(
        modifier: Modifier,
        passwordResetRequestState: State<User>,
        config: Config,
    ) {
        val context = LocalContext.current
        val focusManager = LocalFocusManager.current
        val showProgressBar by remember(passwordResetRequestState) {
            derivedStateOf {
                passwordResetRequestState is State.Loading
            }
        }

        LaunchedEffect(passwordResetRequestState) {
            if (passwordResetRequestState is State.Error) {
                val message = passwordResetRequestState.getMessage(context)
                config.shared.snackbarHostState.showSnackbar(
                    message, duration = SnackbarDuration.Long, withDismissAction = true)
            } else if (passwordResetRequestState is State.Success) {
                passwordResetRequestState.data?.also(config.onPasswordResetRequestSuccess)
            }
        }
        Scaffold(
            topBar = {
                ToolbarWithProgressbar(
                    title = stringResource(R.string.fusers_request_password_reset),
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
                var emailAddress by rememberSaveable(stateSaver = Savers.TEXT_FIELD_VALUE) {
                    mutableStateOf(TextFieldValue())
                }
                val emailAddressError = remember(emailAddress, passwordResetRequestState) {
                    if (emailAddress.text.isBlank()) {
                        context.getString(R.string.fusers_email_required)
                    } else if (!emailAddress.text.matches(Patterns.EMAIL_ADDRESS.toRegex())) {
                        context.getString(R.string.fusers_email_invalid)
                    } else {
                        (passwordResetRequestState as? State.Error)?.let {
                            (it.error as? PasswordResetRequestHttpException)?.email?.joinToString(
                                "\n")
                        } ?: ""
                    }
                }
                val emailAddressHasError by remember(emailAddressError) {
                    derivedStateOf {
                        emailAddressError.isNotBlank()
                    }
                }
                TextInputLayout(
                    modifier = Modifier
                        .fillMaxWidthHorizontalPadding()
                        .padding(top = 16.dp),
                    value = emailAddress,
                    error = emailAddressError,
                    isError = emailAddressHasError,
                    onValueChange = { emailAddress = it },
                    label = stringResource(R.string.fusers_input_field_label_email),
                    keyboardOptions = DefaultKeyboardOptions.copy(
                        imeAction = ImeAction.Done,
                        keyboardType = KeyboardType.Email,
                        capitalization = KeyboardCapitalization.None,
                    ),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                )
                val requiredFields = arrayOf(emailAddress)
                val enableSubmitButton by remember(showProgressBar, *requiredFields) {
                    derivedStateOf {
                        requiredFields.all { it.text.isNotBlank() } && !showProgressBar
                    }
                }
                Button(
                    modifier = Modifier.fillMaxWidthHorizontalPadding(),
                    enabled = enableSubmitButton,
                    onClick = {
                        focusManager.clearFocus()
                        config.onPasswordResetRequest(emailAddress.text.trim())
                    },
                ) {
                    Text(
                        text = stringResource(R.string.fusers_submit_request).toUpperCase(
                            Locale.current),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }

            }
        }
    }
}
