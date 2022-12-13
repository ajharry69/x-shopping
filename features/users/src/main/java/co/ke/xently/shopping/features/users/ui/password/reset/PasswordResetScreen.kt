package co.ke.xently.shopping.features.users.ui.password.reset

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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import co.ke.xently.shopping.features.ui.*
import co.ke.xently.shopping.features.users.R
import co.ke.xently.shopping.features.users.UsersNavGraph
import co.ke.xently.shopping.features.users.UsersNavigator
import co.ke.xently.shopping.features.users.repositories.exceptions.PasswordResetHttpException
import co.ke.xently.shopping.features.users.ui.PasswordVisibilityToggle
import co.ke.xently.shopping.features.users.ui.destinations.VerificationScreenDestination
import co.ke.xently.shopping.features.utils.Shared
import co.ke.xently.shopping.features.utils.State
import co.ke.xently.shopping.libraries.data.source.User
import com.ramcosta.composedestinations.annotation.Destination

object PasswordResetScreen {
    @Stable
    internal data class Config(
        val shared: Shared = Shared(),
        val onPasswordReset: (User.ResetPassword) -> Unit = {},
        val onPasswordResetSuccess: (User) -> Unit = {},
        val onResendPasswordResetCodeClicked: () -> Unit = {},
    )

    data class Args(val isChange: Boolean = false)

    @UsersNavGraph
    @Destination(navArgsDelegate = Args::class)
    @Composable
    internal fun PasswordResetScreen(
        args: Args,
        shared: Shared,
        navigator: UsersNavigator,
        viewModel: PasswordResetScreenViewModel = hiltViewModel(),
    ) {
        val passwordResetState by viewModel.passwordResetState.collectAsState(State.Success(null))
        invoke(
            isChange = args.isChange,
            modifier = Modifier.fillMaxSize(),
            passwordResetState = passwordResetState,
            config = Config(
                shared = shared,
                onPasswordReset = viewModel::invoke,
                onPasswordResetSuccess = { user ->
                    if (!user.isVerified) {
                        navigator.navigate(VerificationScreenDestination()) {
                            launchSingleTop = true
                        }
                    } else if (!navigator.navigateToMainScreen()) {
                        navigator.navigateUp()
                    }
                },
            ),
        )
    }

    @Composable
    @VisibleForTesting
    internal operator fun invoke(
        modifier: Modifier,
        isChange: Boolean,
        passwordResetState: State<User>,
        config: Config,
    ) {
        val context = LocalContext.current
        val focusManager = LocalFocusManager.current
        val showProgressIndicator by remember(passwordResetState) {
            derivedStateOf {
                passwordResetState is State.Loading
            }
        }

        LaunchedEffect(passwordResetState) {
            if (passwordResetState is State.Error) {
                val message = passwordResetState.getMessage(context)
                config.shared.snackbarHostState.showSnackbar(
                    message, duration = SnackbarDuration.Long, withDismissAction = true)
            } else if (passwordResetState is State.Success) {
                passwordResetState.data?.also(config.onPasswordResetSuccess)
            }
        }
        Scaffold(
            topBar = {
                TopAppBarWithProgressIndicator(showProgressIndicator = showProgressIndicator) {
                    TopAppBar(
                        title = { Text(stringResource(R.string.fusers_reset_password)) },
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
                val oldPassword = TextFieldConfig(
                    labelId = R.string.fusers_input_field_label_old_password,
                    valueInputs = null,
                    state = passwordResetState,
                ) {
                    (it.error as? PasswordResetHttpException)?.oldPassword?.joinToString("\n")
                }

                var isOldPasswordVisible by rememberSaveable {
                    mutableStateOf(false)
                }
                TextField(
                    modifier = Modifier
                        .fillMaxWidthHorizontalPadding()
                        .padding(top = 16.dp),
                    value = oldPassword.value,
                    isError = oldPassword.hasError,
                    onValueChange = oldPassword.onValueChange,
                    label = { Text(oldPassword.label) },
                    trailingIcon = {
                        PasswordVisibilityToggle(isVisible = isOldPasswordVisible) {
                            isOldPasswordVisible = !isOldPasswordVisible
                        }
                    },
                    visualTransformation = if (isOldPasswordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    keyboardOptions = DefaultKeyboardOptions.copy(
                        autoCorrect = false,
                        imeAction = ImeAction.Next,
                        keyboardType = KeyboardType.Password,
                    ),
                    supportingText = {
                        SupportingText(oldPassword)
                    },
                )

                val newPassword = TextFieldConfig(
                    labelId = R.string.fusers_input_field_label_new_password,
                    valueInputs = null,
                    state = passwordResetState,
                ) {
                    (it.error as? PasswordResetHttpException)?.newPassword?.joinToString("\n")
                }

                var isNewPasswordVisible by rememberSaveable {
                    mutableStateOf(false)
                }
                TextField(
                    modifier = Modifier.fillMaxWidthHorizontalPadding(),
                    value = newPassword.value,
                    isError = newPassword.hasError,
                    onValueChange = newPassword.onValueChange,
                    label = { Text(newPassword.label) },
                    trailingIcon = {
                        PasswordVisibilityToggle(isVisible = isNewPasswordVisible) {
                            isNewPasswordVisible = !isNewPasswordVisible
                        }
                    },
                    visualTransformation = if (isNewPasswordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    keyboardOptions = DefaultKeyboardOptions.copy(
                        autoCorrect = false,
                        imeAction = ImeAction.Done,
                        keyboardType = KeyboardType.Password,
                    ),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                )
                val requiredFields = arrayOf(oldPassword, newPassword)
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
                        val resetPassword = User.ResetPassword(
                            isChange = isChange,
                            oldPassword = oldPassword.value.text.trim(),
                            newPassword = newPassword.value.text.trim(),
                        )
                        config.onPasswordReset(resetPassword)
                    },
                ) {
                    Text(
                        text = stringResource(R.string.fusers_reset_password).toUpperCase(
                            Locale.current),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }

            }
        }
    }
}
