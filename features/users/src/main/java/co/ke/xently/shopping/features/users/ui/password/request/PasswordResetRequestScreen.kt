package co.ke.xently.shopping.features.users.ui.password.request

import android.util.Patterns
import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import co.ke.xently.shopping.features.ui.*
import co.ke.xently.shopping.features.users.R
import co.ke.xently.shopping.features.users.UsersNavGraph
import co.ke.xently.shopping.features.users.repositories.exceptions.PasswordResetRequestHttpException
import co.ke.xently.shopping.features.users.ui.destinations.PasswordResetScreenDestination
import co.ke.xently.shopping.features.utils.Shared
import co.ke.xently.shopping.features.utils.State
import co.ke.xently.shopping.libraries.data.source.User
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

internal object PasswordResetRequestScreen {
    @Stable
    data class Config(
        val shared: Shared = Shared(),
        val onPasswordResetRequest: (String) -> Unit = {},
        val onPasswordResetRequestSuccess: (User) -> Unit = {},
        val onResendPasswordResetRequestCodeClicked: () -> Unit = {},
    )

    @UsersNavGraph
    @Destination
    @Composable
    fun PasswordResetRequestScreen(
        shared: Shared,
        navigator: DestinationsNavigator,
        viewModel: PasswordResetRequestScreenViewModel = hiltViewModel(),
    ) {
        val passwordResetRequestState by viewModel.passwordResetRequestState.collectAsState(
            State.Success(null))
        PasswordResetRequestScreen(
            modifier = Modifier.fillMaxSize(),
            passwordResetRequestState = passwordResetRequestState,
            config = Config(
                shared = shared,
                onPasswordResetRequest = viewModel::invoke,
                onPasswordResetRequestSuccess = {
                    navigator.navigate(PasswordResetScreenDestination()) {
                        launchSingleTop = true
                    }
                },
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
        val showProgressIndicator by remember(passwordResetRequestState) {
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
                TopAppBarWithProgressIndicator(showProgressIndicator = showProgressIndicator) {
                    TopAppBar(
                        title = { Text(stringResource(R.string.fusers_request_password_reset)) },
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
                val emailAddress = TextFieldConfig(
                    labelId = R.string.fusers_input_field_label_email,
                    valueInputs = null,
                    state = passwordResetRequestState,
                    extraErrorChecks = {
                        if (!it.text.matches(Patterns.EMAIL_ADDRESS.toRegex())) {
                            context.getString(R.string.fusers_email_invalid)
                        } else null
                    },
                ) {
                    (it.error as? PasswordResetRequestHttpException)?.email?.joinToString("\n")
                }

                TextField(
                    modifier = Modifier
                        .fillMaxWidthHorizontalPadding()
                        .padding(top = 16.dp),
                    value = emailAddress.value,
                    isError = emailAddress.hasError,
                    onValueChange = emailAddress.onValueChange,
                    label = { Text(emailAddress.label) },
                    keyboardOptions = DefaultKeyboardOptions.copy(
                        imeAction = ImeAction.Done,
                        keyboardType = KeyboardType.Email,
                        capitalization = KeyboardCapitalization.None,
                    ),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                )
                val requiredFields = arrayOf(emailAddress)
                val enableSubmitButton by remember(showProgressIndicator, *requiredFields) {
                    derivedStateOf {
                        requiredFields.all { !it.hasError } && !showProgressIndicator
                    }
                }
                Button(
                    modifier = Modifier.fillMaxWidthHorizontalPadding(),
                    enabled = enableSubmitButton,
                    onClick = {
                        focusManager.clearFocus()
                        config.onPasswordResetRequest(emailAddress.value.text.trim())
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
