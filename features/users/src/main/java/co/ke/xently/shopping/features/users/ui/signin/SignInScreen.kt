package co.ke.xently.shopping.features.users.ui.signin

import android.content.Intent
import android.util.Patterns
import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.*
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import co.ke.xently.shopping.features.ui.*
import co.ke.xently.shopping.features.users.BasicAuth
import co.ke.xently.shopping.features.users.R
import co.ke.xently.shopping.features.users.repositories.exceptions.SignInHttpException
import co.ke.xently.shopping.features.utils.Routes
import co.ke.xently.shopping.features.utils.Shared
import co.ke.xently.shopping.features.utils.State
import co.ke.xently.shopping.libraries.data.source.User

internal object SignInScreen {
    data class Config(
        val shared: Shared = Shared(),
        val onSignUp: (User) -> Unit = {},
        val onSignIn: (BasicAuth) -> Unit = {},
        val onSignInSuccess: (User) -> Unit = {},
        val onForgotPasswordClicked: () -> Unit = {},
    )

    @Composable
    operator fun invoke(
        modifier: Modifier,
        config: Config,
        viewModel: SignInScreenViewModel = hiltViewModel(),
    ) {
        val signInState by viewModel.signInState.collectAsState(State.Success(null))
        SignInScreen(
            modifier = modifier,
            signInState = signInState,
            config = config.copy(
                onSignIn = viewModel::invoke,
            ),
        )
    }

    @Composable
    @VisibleForTesting
    operator fun invoke(modifier: Modifier, signInState: State<User>, config: Config) {
        val context = LocalContext.current
        val focusManager = LocalFocusManager.current
        val showProgressBar by remember(signInState) {
            derivedStateOf {
                signInState is State.Loading
            }
        }

        LaunchedEffect(signInState) {
            if (signInState is State.Error) {
                val message = signInState.getMessage(context)
                config.shared.snackbarHostState.showSnackbar(
                    message, duration = SnackbarDuration.Long, withDismissAction = true)
            } else if (signInState is State.Success) {
                signInState.data?.also(config.onSignInSuccess)
            }
        }
        Scaffold(
            topBar = {
                ToolbarWithProgressbar(
                    title = stringResource(R.string.fusers_sign_in),
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
                var username by rememberSaveable(stateSaver = Savers.TEXT_FIELD_VALUE) {
                    mutableStateOf(TextFieldValue())
                }
                val usernameError = remember(username, signInState) {
                    if (username.text.isBlank()) {
                        context.getString(R.string.fusers_email_required)
                    } else if (!username.text.matches(Patterns.EMAIL_ADDRESS.toRegex())) {
                        context.getString(R.string.fusers_email_invalid)
                    } else {
                        (signInState as? State.Error)?.let {
                            (it.error as? SignInHttpException)?.email?.joinToString("\n")
                        } ?: ""
                    }
                }
                val usernameHasError by remember(usernameError) {
                    derivedStateOf {
                        usernameError.isNotBlank()
                    }
                }
                TextInputLayout(
                    modifier = Modifier.fillMaxWidthHorizontalPadding(),
                    value = username,
                    error = usernameError,
                    isError = usernameHasError,
                    onValueChange = { username = it },
                    label = stringResource(R.string.fusers_sign_in_input_field_label_username),
                    keyboardOptions = DefaultKeyboardOptions.copy(
                        keyboardType = KeyboardType.Email,
                        capitalization = KeyboardCapitalization.None,
                    ),
                )
                var password by rememberSaveable(stateSaver = Savers.TEXT_FIELD_VALUE) {
                    mutableStateOf(TextFieldValue())
                }
                val passwordError = remember(password, signInState) {
                    if (password.text.isBlank()) {
                        context.getString(R.string.fusers_password_required)
                    } else {
                        (signInState as? State.Error)?.let {
                            (it.error as? SignInHttpException)?.password?.joinToString("\n")
                        } ?: ""
                    }
                }
                val passwordHasError by remember(passwordError) {
                    derivedStateOf {
                        passwordError.isNotBlank()
                    }
                }
                var isPasswordVisible by rememberSaveable {
                    mutableStateOf(false)
                }
                TextInputLayout(
                    modifier = Modifier.fillMaxWidthHorizontalPadding(),
                    value = password,
                    error = passwordError,
                    isError = passwordHasError,
                    onValueChange = { password = it },
                    label = stringResource(R.string.fusers_input_field_label_password),
                    trailingIcon = {
                        PasswordVisibilityToggle(isVisible = isPasswordVisible) {
                            isPasswordVisible = !isPasswordVisible
                        }
                    },
                    visualTransformation = if (isPasswordVisible) {
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
                Row(
                    modifier = Modifier
                        .fillMaxWidthHorizontalPadding()
                        .height(IntrinsicSize.Min),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    TextButton(onClick = config.onForgotPasswordClicked) {
                        Text(text = stringResource(R.string.fusers_button_label_forgot_password))
                    }
                    val requiredFields = arrayOf(
                        username,
                        password,
                    )
                    val enableSubmitButton by remember(showProgressBar, *requiredFields) {
                        derivedStateOf {
                            requiredFields.all { it.text.isNotBlank() } && !showProgressBar
                        }
                    }
                    Button(
                        enabled = enableSubmitButton,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            focusManager.clearFocus()
                            config.onSignIn(
                                BasicAuth(
                                    password = password.text,
                                    username = username.text.trim(),
                                )
                            )
                        },
                    ) {
                        Text(
                            text = stringResource(R.string.fusers_sign_in).toUpperCase(
                                Locale.current),
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidthHorizontalPadding(),
                ) {
                    Divider(modifier = Modifier.weight(1f))
                    Text(
                        text = stringResource(R.string.fusers_or),
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                    Divider(modifier = Modifier.weight(1f))
                }
                val signUpText = buildAnnotatedString {
                    append("${stringResource(R.string.fusers_dont_have_account_question_prefix)}, ")
                    pushStringAnnotation(
                        tag = "SIGNUP_DEEPLINK",
                        annotation = Routes.Users.Deeplinks.SIGN_UP,
                    )
                    withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                        append(context.getString(R.string.fusers_sign_up)
                            .toLowerCase(Locale.current))
                    }
                    pop()
                    append('.')
                }
                ClickableText(
                    text = signUpText,
                    onClick = { offset ->
                        signUpText.getStringAnnotations("SIGNUP_DEEPLINK", offset, offset)
                            .firstOrNull()
                            ?.let { annotation ->
                                Intent(Intent.ACTION_VIEW, annotation.item.toUri()).also {
                                    context.startActivity(it)
                                }
                            }
                    },
                    modifier = Modifier.fillMaxWidthHorizontalPadding(),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground,
                    ),
                )

            }
        }
    }
}
