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
import co.ke.xently.shopping.features.users.R
import co.ke.xently.shopping.features.users.UsersNavGraph
import co.ke.xently.shopping.features.users.UsersNavigator
import co.ke.xently.shopping.features.users.repositories.exceptions.SignUpHttpException
import co.ke.xently.shopping.features.users.ui.PasswordVisibilityToggle
import co.ke.xently.shopping.features.users.ui.destinations.VerificationScreenDestination
import co.ke.xently.shopping.features.utils.Deeplinks
import co.ke.xently.shopping.features.utils.Shared
import co.ke.xently.shopping.features.utils.State
import co.ke.xently.shopping.libraries.data.source.User
import com.ramcosta.composedestinations.annotation.DeepLink
import com.ramcosta.composedestinations.annotation.Destination

internal object SignUpScreen {
    @Stable
    data class Config(
        val shared: Shared = Shared(),
        val onSubmitDetails: (User) -> Unit = {},
        val onSignUpSuccess: (User) -> Unit = {},
    )

    @UsersNavGraph
    @Destination(
        deepLinks = [
            DeepLink(uriPattern = Deeplinks.SIGN_UP),
        ],
    )
    @Composable
    fun SignUpScreen(
        shared: Shared,
        navigator: UsersNavigator,
        viewModel: SignUpScreenViewModel = hiltViewModel(),
    ) {
        val signUpState by viewModel.signUpState.collectAsState(State.Success(null))
        SignUpScreen(
            modifier = Modifier.fillMaxSize(),
            signUpState = signUpState,
            config = Config(
                shared = shared,
                onSubmitDetails = viewModel::save,
                onSignUpSuccess = {
                    when {
                        !it.isVerified -> {
                            navigator.navigate(VerificationScreenDestination()) {
                                launchSingleTop = true
                            }
                        }
                        !navigator.navigateToMainScreen() -> {
                            navigator.navigateUp()
                        }
                    }
                },
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
        val showProgressIndicator by remember(signUpState) {
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
                TopAppBarWithProgressIndicator(showProgressIndicator = showProgressIndicator) {
                    TopAppBar(
                        title = { Text(stringResource(R.string.fusers_sign_up)) },
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
                val name = TextFieldConfig(
                    labelId = R.string.fusers_input_field_label_name,
                    valueInputs = null,
                    state = signUpState,
                ) {
                    (it.error as? SignUpHttpException)?.name?.joinToString("\n")
                }
                TextField(
                    modifier = Modifier
                        .fillMaxWidthHorizontalPadding()
                        .padding(top = 16.dp),
                    value = name.value,
                    isError = name.hasError,
                    onValueChange = name.onValueChange,
                    label = { Text(name.label) },
                    keyboardOptions = DefaultKeyboardOptions.copy(capitalization = KeyboardCapitalization.Words),
                    supportingText = {
                        SupportingText(config = name)
                    },
                )

                val email = TextFieldConfig(
                    labelId = R.string.fusers_input_field_label_email,
                    valueInputs = null,
                    state = signUpState,
                    extraErrorChecks = {
                        if (!it.text.matches(Patterns.EMAIL_ADDRESS.toRegex())) {
                            context.getString(R.string.fusers_email_invalid)
                        } else null
                    },
                ) {
                    (it.error as? SignUpHttpException)?.email?.joinToString("\n")
                }

                TextField(
                    modifier = Modifier.fillMaxWidthHorizontalPadding(),
                    value = email.value,
                    isError = email.hasError,
                    onValueChange = email.onValueChange,
                    label = { Text(email.label) },
                    keyboardOptions = DefaultKeyboardOptions.copy(
                        keyboardType = KeyboardType.Email,
                        capitalization = KeyboardCapitalization.None,
                    ),
                    supportingText = {
                        SupportingText(config = email)
                    },
                )

                val password = TextFieldConfig(
                    labelId = R.string.fusers_input_field_label_password,
                    valueInputs = null,
                    state = signUpState,
                ) {
                    (it.error as? SignUpHttpException)?.password?.joinToString("\n")
                }
                var isPasswordVisible by rememberSaveable {
                    mutableStateOf(false)
                }
                TextField(
                    modifier = Modifier.fillMaxWidthHorizontalPadding(),
                    value = password.value,
                    isError = password.hasError,
                    onValueChange = password.onValueChange,
                    label = { Text(password.label) },
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
                    supportingText = {
                        SupportingText(config = password)
                    },
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
                val requiredFields = arrayOf(name, email, password)
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
                        config.onSubmitDetails.invoke(
                            (user ?: User.DEFAULT_INSTANCE).copy(
                                name = name.value.text.trim(),
                                email = email.value.text.trim(),
                                password = password.value.text.trim(),
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
