package co.ke.xently.shopping

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toUpperCase
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavHostController
import co.ke.xently.shopping.features.ui.theme.XentlyTheme
import co.ke.xently.shopping.features.users.ui.destinations.SignInScreenDestination
import co.ke.xently.shopping.features.users.ui.destinations.VerificationScreenDestination
import co.ke.xently.shopping.features.utils.Shared
import co.ke.xently.shopping.features.utils.State
import co.ke.xently.shopping.libraries.data.source.User
import co.ke.xently.shopping.navigation.Navigator
import co.ke.xently.shopping.navigation.RootNavGraph
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.animations.rememberAnimatedNavHostEngine
import com.ramcosta.composedestinations.navigation.dependency
import com.ramcosta.composedestinations.navigation.navigate
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * We are extending from `FragmentActivity` because the app needs a `FragmentManager` to shown
 * `MaterialAlertDialog`s in some screens within the application.
 */
@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    // Exposed to share state between screens
    private val viewModel by viewModels<MainActivityViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            XentlyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    val engine = rememberAnimatedNavHostEngine()
                    val controller = engine.rememberNavController()
                    val composeCoroutineScope = rememberCoroutineScope()
                    val snackbarHostState = remember {
                        SnackbarHostState()
                    }

                    val (_, user) = isLoadingWithUser(snackbarHostState, controller)

                    DestinationsNavHost(
                        engine = engine,
                        navGraph = RootNavGraph,
                        navController = controller,
                        dependenciesContainerBuilder = {
                            val shared = Shared(
                                user = user,
                                snackbarHostState = snackbarHostState,
                                onNavigationIconClicked = onBackPressedDispatcher::onBackPressed,
                                onAuthenticationRequired = {
                                    destinationsNavigator.navigate(SignInScreenDestination()) {
                                        launchSingleTop = true
                                    }
                                },
                                onAuthenticationExpected = { isNotificationDismissible ->
                                    val message =
                                        getString(R.string.authentication_session_expired_resign_in)
                                    val actionLabel =
                                        getString(R.string.sign_in).toUpperCase(Locale.current)
                                    composeCoroutineScope.launch {
                                        val result = snackbarHostState.showSnackbar(
                                            message,
                                            actionLabel = actionLabel,
                                            withDismissAction = isNotificationDismissible,
                                            duration = SnackbarDuration.Long,
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            destinationsNavigator.navigate(SignInScreenDestination()) {
                                                launchSingleTop = true
                                            }
                                        }
                                    }
                                },
                            )
                            dependency(shared)
                            dependency(viewModel)
                            dependency(Navigator(destinationsNavigator))
                        },
                    )
                }
            }
        }
    }

    @Composable
    private fun isLoadingWithUser(state: SnackbarHostState, controller: NavHostController): Pair<Boolean, User?> {
        val userState by viewModel.userState.collectAsState(initial = State.Success(null))

        LaunchedEffect(userState) {
            when (userState) {
                is State.Error -> {
                    val message =
                        (userState as State.Error).getMessage(this@MainActivity)
                    state.showSnackbar(
                        message = message,
                        withDismissAction = true,
                        duration = SnackbarDuration.Indefinite,
                    )
                }
                State.Loading -> {
                    Timber.i("onCreate: user state loading...")
                }
                is State.Success -> {
                    (userState as State.Success).data?.let {
                        if (!it.isVerified) {
                            controller.navigate(VerificationScreenDestination()) {
                                launchSingleTop = true
                            }
                        }
                    }
                }
            }
        }

        val signOutState by viewModel.signOutState.collectAsState(
            initial = State.Success(null))

        LaunchedEffect(signOutState) {
            if (signOutState is State.Error || (signOutState as? State.Success)?.data != null) {
                controller.navigate(SignInScreenDestination()) {
                    launchSingleTop = true
                }
            }
        }

        val user: User? by remember(userState) {
            derivedStateOf {
                (userState as? State.Success)?.data
            }
        }
        val showProgressbar: Boolean by remember(signOutState) {
            derivedStateOf {
                signOutState is State.Loading
            }
        }
        return showProgressbar to user
    }
}
