package co.ke.xently.shopping.features.users.ui.signup

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import co.ke.xently.shopping.features.ui.*
import co.ke.xently.shopping.features.ui.TextInputLayout.DefaultKeyboardOptions
import co.ke.xently.shopping.features.users.R
import co.ke.xently.shopping.features.users.repositories.exceptions.SignUpHttpException
import co.ke.xently.shopping.features.utils.Shared
import co.ke.xently.shopping.features.utils.State
import co.ke.xently.shopping.libraries.data.source.User

internal object SignUpScreen {
    data class Config(
        val shared: Shared = Shared(),
        val onSubmitDetails: (User) -> Unit = {},
        val onSignUpSuccess: (User) -> Unit = {},
    )

    @Composable
    operator fun invoke(
        modifier: Modifier,
        config: Config,
        viewModel: SignUpScreenViewModel = hiltViewModel(),
    ) {
        val signUpState by viewModel.signUpState.collectAsState(State.Success(null))
        SignUpScreen(
            modifier = modifier,
            signUpState = signUpState,
            config = config.copy(
                onSubmitDetails = viewModel::save,
            ),
        )
    }

    @Composable
    @VisibleForTesting
    operator fun invoke(modifier: Modifier, signUpState: State<User>, config: Config) {
        val user by remember(signUpState) {
            derivedStateOf {
                (signUpState as? State.Success)?.data
            }
        }
        val context = LocalContext.current
        val focusManager = LocalFocusManager.current
        val showProgressBar by remember(signUpState) {
            derivedStateOf {
                signUpState is State.Loading
            }
        }

        LaunchedEffect(signUpState) {
            if (signUpState is State.Error) {
                val message = signUpState.getMessage(context)
                config.shared.snackbarHostState.showSnackbar(
                    message, duration = SnackbarDuration.Long, withDismissAction = true)
            } else if (signUpState is State.Success) {
                signUpState.data?.also(config.onSignUpSuccess)
            }
        }
        Scaffold(
            topBar = {
                ToolbarWithProgressbar(
                    title = stringResource(R.string.fusers_sign_up),
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
                var name by rememberSaveable(stateSaver = Savers.TEXT_FIELD_VALUE) {
                    mutableStateOf(TextFieldValue())
                }
                val nameError = remember(name, signUpState) {
                    (signUpState as? State.Error)?.let {
                        (it.error as? SignUpHttpException)?.name?.joinToString("\n")
                    } ?: ""
                }
                val nameHasError by remember(nameError) {
                    derivedStateOf {
                        nameError.isNotBlank()
                    }
                }
                TextInputLayout(
                    modifier = Modifier
                        .fillMaxWidthHorizontalPadding()
                        .padding(top = 16.dp),
                    value = name,
                    error = nameError,
                    isError = nameHasError,
                    onValueChange = { name = it },
                    label = stringResource(R.string.fusers_input_field_label_name),
                    keyboardOptions = DefaultKeyboardOptions.copy(capitalization = KeyboardCapitalization.Words),
                )

                var email by rememberSaveable(stateSaver = Savers.TEXT_FIELD_VALUE) {
                    mutableStateOf(TextFieldValue())
                }
                val emailError = remember(email, signUpState) {
                    if (email.text.isBlank()) {
                        context.getString(R.string.fusers_email_required)
                    } else if (!email.text.matches(Patterns.EMAIL_ADDRESS.toRegex())) {
                        context.getString(R.string.fusers_email_invalid)
                    } else {
                        (signUpState as? State.Error)?.let {
                            (it.error as? SignUpHttpException)?.email?.joinToString("\n")
                        } ?: ""
                    }
                }
                val emailHasError by remember(emailError) {
                    derivedStateOf {
                        emailError.isNotBlank()
                    }
                }
                TextInputLayout(
                    modifier = Modifier.fillMaxWidthHorizontalPadding(),
                    value = email,
                    error = emailError,
                    isError = emailHasError,
                    onValueChange = { email = it },
                    label = stringResource(R.string.fusers_input_field_label_email),
                    keyboardOptions = DefaultKeyboardOptions.copy(
                        keyboardType = KeyboardType.Email,
                        capitalization = KeyboardCapitalization.None,
                    ),
                )

                var password by rememberSaveable(stateSaver = Savers.TEXT_FIELD_VALUE) {
                    mutableStateOf(TextFieldValue())
                }
                val passwordError = remember(password, signUpState) {
                    if (password.text.isBlank()) {
                        context.getString(R.string.fusers_password_required)
                    } else {
                        (signUpState as? State.Error)?.let {
                            (it.error as? SignUpHttpException)?.password?.joinToString("\n")
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
                val termsText = buildAnnotatedString {
                    append("${stringResource(R.string.fusers_terms_and_conditions_prefix)} ")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("${stringResource(R.string.fusers_sign_up).toUpperCase(Locale.current)}, ")
                    }
                    append(" ${stringResource(R.string.fusers_terms_and_conditions_agree)} ")
                    pushStringAnnotation(
                        tag = "TOS_URL",
                        annotation = "https://pos.xently.co.ke/terms-and-conditions/",
                    )
                    withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                        append(context.getString(R.string.fusers_terms_and_conditions_terms_of_service))
                    }
                    pop()
                    append(" & ")
                    pushStringAnnotation(
                        tag = "PRIVACY_POLICY_URL",
                        annotation = "https://pos.xently.co.ke/privacy-policy/",
                    )
                    withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                        append(context.getString(R.string.fusers_terms_and_conditions_privacy_policy))
                    }
                    append(".")
                    pop()
                }
                ClickableText(
                    text = termsText,
                    modifier = Modifier.fillMaxWidthHorizontalPadding(),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onBackground,
                    ),
                ) { offset ->
                    termsText.getStringAnnotations("TOS_URL", offset, offset).firstOrNull()
                        ?.let { annotation ->
                            Intent(Intent.ACTION_VIEW, annotation.item.toUri()).also {
                                context.startActivity(it)
                            }
                        } ?: termsText.getStringAnnotations("PRIVACY_POLICY_URL", offset, offset)
                        .firstOrNull()
                        ?.let { annotation ->
                            Intent(Intent.ACTION_VIEW, annotation.item.toUri()).also {
                                context.startActivity(it)
                            }
                        }
                }
                val requiredFields = arrayOf(
                    email,
                    password,
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
                            (user ?: User.DEFAULT_INSTANCE).copy(
                                name = name.text.trim(),
                                email = email.text.trim(),
                                password = password.text.trim(),
                            )
                        )
                    },
                ) {
                    Text(
                        text = stringResource(R.string.fusers_sign_up).toUpperCase(Locale.current),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}
