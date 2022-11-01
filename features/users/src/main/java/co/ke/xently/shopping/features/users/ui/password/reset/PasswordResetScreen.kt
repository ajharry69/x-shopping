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
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import co.ke.xently.shopping.features.ui.*
import co.ke.xently.shopping.features.ui.TextInputLayout.DefaultKeyboardOptions
import co.ke.xently.shopping.features.users.R
import co.ke.xently.shopping.features.users.repositories.exceptions.PasswordResetHttpException
import co.ke.xently.shopping.features.utils.Shared
import co.ke.xently.shopping.features.utils.State
import co.ke.xently.shopping.libraries.data.source.User

internal object PasswordResetScreen {
    data class Config(
        val shared: Shared = Shared(),
        val onPasswordReset: (User.ResetPassword) -> Unit = {},
        val onPasswordResetSuccess: (User) -> Unit = {},
        val onResendPasswordResetCodeClicked: () -> Unit = {},
    )

    @Composable
    operator fun invoke(
        modifier: Modifier,
        config: Config,
        isChange: Boolean = false,
        viewModel: PasswordResetScreenViewModel = hiltViewModel(),
    ) {
        val passwordResetState by viewModel.passwordResetState.collectAsState(State.Success(null))
        PasswordResetScreen(
            modifier = modifier,
            isChange = isChange,
            passwordResetState = passwordResetState,
            config = config.copy(
                onPasswordReset = viewModel::invoke,
            ),
        )
    }

    @Composable
    @VisibleForTesting
    operator fun invoke(
        modifier: Modifier,
        isChange: Boolean,
        passwordResetState: State<User>,
        config: Config,
    ) {
        val context = LocalContext.current
        val focusManager = LocalFocusManager.current
        val showProgressBar by remember(passwordResetState) {
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
                ToolbarWithProgressbar(
                    title = stringResource(R.string.fusers_reset_password),
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
                var oldPassword by rememberSaveable(stateSaver = Savers.TEXT_FIELD_VALUE) {
                    mutableStateOf(TextFieldValue())
                }
                val oldPasswordError = remember(oldPassword, passwordResetState) {
                    if (oldPassword.text.isBlank()) {
                        context.getString(R.string.fusers_old_password_required)
                    } else {
                        (passwordResetState as? State.Error)?.let {
                            (it.error as? PasswordResetHttpException)?.oldPassword?.joinToString(
                                "\n")
                        } ?: ""
                    }
                }
                val oldPasswordHasError by remember(oldPasswordError) {
                    derivedStateOf {
                        oldPasswordError.isNotBlank()
                    }
                }
                var isOldPasswordVisible by rememberSaveable {
                    mutableStateOf(false)
                }
                TextInputLayout(
                    modifier = Modifier
                        .fillMaxWidthHorizontalPadding()
                        .padding(top = 16.dp),
                    value = oldPassword,
                    error = oldPasswordError,
                    isError = oldPasswordHasError,
                    onValueChange = { oldPassword = it },
                    label = stringResource(R.string.fusers_input_field_label_old_password),
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
                )
                var newPassword by rememberSaveable(stateSaver = Savers.TEXT_FIELD_VALUE) {
                    mutableStateOf(TextFieldValue())
                }
                val newPasswordError = remember(newPassword, passwordResetState) {
                    if (newPassword.text.isBlank()) {
                        context.getString(R.string.fusers_new_password_required)
                    } else {
                        (passwordResetState as? State.Error)?.let {
                            (it.error as? PasswordResetHttpException)?.newPassword?.joinToString(
                                "\n")
                        } ?: ""
                    }
                }
                val newPasswordHasError by remember(newPasswordError) {
                    derivedStateOf {
                        newPasswordError.isNotBlank()
                    }
                }
                var isNewPasswordVisible by rememberSaveable {
                    mutableStateOf(false)
                }
                TextInputLayout(
                    modifier = Modifier.fillMaxWidthHorizontalPadding(),
                    value = newPassword,
                    error = newPasswordError,
                    isError = newPasswordHasError,
                    onValueChange = { newPassword = it },
                    label = stringResource(R.string.fusers_input_field_label_new_password),
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
                        val resetPassword = User.ResetPassword(
                            isChange = isChange,
                            oldPassword = oldPassword.text.trim(),
                            newPassword = newPassword.text.trim(),
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
