package co.ke.xently.shopping.features.users.ui.verification

import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import co.ke.xently.shopping.features.ui.*
import co.ke.xently.shopping.features.ui.TextInputLayout.DefaultKeyboardOptions
import co.ke.xently.shopping.features.users.R
import co.ke.xently.shopping.features.users.repositories.exceptions.VerificationHttpException
import co.ke.xently.shopping.features.utils.Shared
import co.ke.xently.shopping.features.utils.State
import co.ke.xently.shopping.libraries.data.source.User
import co.ke.xently.shopping.libraries.data.source.remote.HttpException.Companion.requiresAuthentication
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

internal object VerificationScreen {
    private const val DEFAULT_RESEND_COUNTDOWN = 70
    private const val DEFAULT_SHOW_RESEND_UI_ON_COUNTDOWN = 60

    data class Config(
        val shared: Shared = Shared(),
        val onVerification: (String) -> Unit = {},
        val onVerificationSuccess: (User) -> Unit = {},
        val onResendVerificationCodeClicked: () -> Unit = {},
    )

    @Composable
    operator fun invoke(
        modifier: Modifier,
        config: Config,
        viewModel: VerificationScreenViewModel = hiltViewModel(),
    ) {
        val verificationState by viewModel.verificationState.collectAsState(
            State.Success(null))
        val resendVerificationCodeState by viewModel.resendVerificationCodeState.collectAsState(
            State.Success(null))
        VerificationScreen(
            modifier = modifier,
            verificationState = verificationState,
            resendVerificationCodeState = resendVerificationCodeState,
            config = config.copy(
                onVerification = viewModel::invoke,
                onResendVerificationCodeClicked = viewModel::resendCode,
            ),
        )
    }

    @Composable
    @VisibleForTesting
    operator fun invoke(
        modifier: Modifier,
        verificationState: State<User>,
        resendVerificationCodeState: State<User>,
        config: Config,
    ) {
        val context = LocalContext.current
        val focusManager = LocalFocusManager.current
        val showProgressBar by remember(verificationState, resendVerificationCodeState) {
            derivedStateOf {
                verificationState is State.Loading
                        || resendVerificationCodeState is State.Loading
            }
        }

        LaunchedEffect(resendVerificationCodeState) {
            (resendVerificationCodeState as? State.Error)?.run {
                if (error.requiresAuthentication()) {
                    config.shared.onAuthenticationRequired()
                }
            }
        }

        LaunchedEffect(verificationState) {
            if (verificationState is State.Error) {
                val message = verificationState.getMessage(context)
                config.shared.snackbarHostState.showSnackbar(
                    message, duration = SnackbarDuration.Long, withDismissAction = true)
            } else if (verificationState is State.Success) {
                verificationState.data?.also(config.onVerificationSuccess)
            }
        }
        Scaffold(
            topBar = {
                ToolbarWithProgressbar(
                    title = stringResource(R.string.fusers_verify_account),
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
                var verificationCode by rememberSaveable(stateSaver = Savers.TEXT_FIELD_VALUE) {
                    mutableStateOf(TextFieldValue())
                }
                val verificationCodeError = remember(verificationCode, verificationState) {
                    if (verificationCode.text.isBlank()) {
                        context.getString(R.string.fusers_verification_code_required)
                    } else {
                        (verificationState as? State.Error)?.let {
                            (it.error as? VerificationHttpException)?.code?.joinToString("\n")
                        } ?: ""
                    }
                }
                val verificationCodeHasError by remember(verificationCodeError) {
                    derivedStateOf {
                        verificationCodeError.isNotBlank()
                    }
                }
                TextInputLayout(
                    modifier = Modifier
                        .fillMaxWidthHorizontalPadding()
                        .padding(top = 16.dp),
                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center).merge(
                        MaterialTheme.typography.headlineSmall,
                    ),
                    value = verificationCode,
                    error = verificationCodeError,
                    isError = verificationCodeHasError,
                    onValueChange = { verificationCode = it },
                    label = stringResource(R.string.fusers_verification_input_field_label_code),
                    keyboardOptions = DefaultKeyboardOptions.copy(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done,
                    ),
                    visualTransformation = { text ->
                        TransformedText(
                            AnnotatedString(
                                text.text.let {
                                    if (it.length > 5) {
                                        it.slice(0..5)
                                    } else {
                                        it
                                    }
                                }.toCharArray().joinToString("-"),
                            ),
                            OffsetMapping.Identity,
                        )
                    },
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidthHorizontalPadding()
                        .height(IntrinsicSize.Min),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    var resendCountDown by rememberSaveable {
                        mutableStateOf(DEFAULT_RESEND_COUNTDOWN)
                    }
                    var resendButtonClicked by remember {
                        mutableStateOf(true)
                    }
                    LaunchedEffect(resendButtonClicked) {
                        repeat(DEFAULT_RESEND_COUNTDOWN) {
                            delay(1.seconds)
                            resendCountDown = DEFAULT_RESEND_COUNTDOWN - (it + 1)
                        }
                    }
                    val hideResendVerificationCodeUI by remember(resendCountDown) {
                        derivedStateOf {
                            resendCountDown > DEFAULT_SHOW_RESEND_UI_ON_COUNTDOWN
                        }
                    }
                    if (!hideResendVerificationCodeUI) {
                        val showResendCountDown by remember(resendCountDown) {
                            derivedStateOf {
                                resendCountDown > 0
                            }
                        }
                        val (label, enabled, onClick) = if (showResendCountDown) {
                            Triple(stringResource(R.string.fusers_button_label_resend_verification_code_count_down,
                                resendCountDown), false) {}
                        } else {
                            Triple(stringResource(R.string.fusers_button_label_resend_verification_code).toUpperCase(
                                Locale.current), true) {
                                config.onResendVerificationCodeClicked()
                                resendButtonClicked = !resendButtonClicked
                            }
                        }
                        OutlinedButton(
                            enabled = enabled,
                            onClick = onClick,
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(text = label)
                        }
                    }

                    val requiredFields = arrayOf(verificationCode)
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
                            config.onVerification(verificationCode.text.trim())
                        },
                    ) {
                        Text(
                            text = stringResource(R.string.fusers_verify).toUpperCase(
                                Locale.current),
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
            }
        }
    }
}