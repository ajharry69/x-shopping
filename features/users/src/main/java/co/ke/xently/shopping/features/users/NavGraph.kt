package co.ke.xently.shopping.features.users

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.*
import co.ke.xently.shopping.features.users.ui.password.request.PasswordResetRequestScreen
import co.ke.xently.shopping.features.users.ui.password.reset.PasswordResetScreen
import co.ke.xently.shopping.features.users.ui.signin.SignInScreen
import co.ke.xently.shopping.features.users.ui.signup.SignUpScreen
import co.ke.xently.shopping.features.users.ui.verification.VerificationScreen
import co.ke.xently.shopping.features.utils.Routes
import co.ke.xently.shopping.features.utils.Shared
import co.ke.xently.shopping.features.utils.buildRoute
import com.google.accompanist.navigation.animation.composable

fun NavGraphBuilder.authenticationGraph(
    shared: Shared,
    navController: NavHostController,
) {
    navigation(route = Routes.Users.toString(), startDestination = Routes.Users.SIGN_IN) {
        composable(
            route = Routes.Users.SIGN_IN,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = Routes.Users.Deeplinks.SIGN_IN
                },
            ),
        ) {
            SignInScreen(
                modifier = Modifier.fillMaxSize(),
                config = SignInScreen.Config(
                    shared = shared,
                    onSignUp = {
                        navController.navigate(Routes.Users.SIGN_UP.buildRoute()) {
                            launchSingleTop = true
                        }
                    },
                    onForgotPasswordClicked = {
                        navController.navigate(Routes.Users.REQUEST_PASSWORD_RESET.buildRoute()) {
                            launchSingleTop = true
                        }
                    },
                    onSignInSuccess = {
                        if (it.isVerified) {
                            navController.navigateUp()
                        } else {
                            navController.popBackStack() // Don't return to the sign-in screen
                            navController.navigate(Routes.Users.VERIFY_ACCOUNT.buildRoute()) {
                                launchSingleTop = true
                            }
                        }
                    },
                ),
            )
        }
        composable(
            route = Routes.Users.SIGN_UP,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = Routes.Users.Deeplinks.SIGN_UP
                },
            ),
        ) {
            SignUpScreen(
                modifier = Modifier.fillMaxSize(),
                config = SignUpScreen.Config(
                    shared = shared,
                    onSignUpSuccess = {
                        when {
                            !it.isVerified -> {
                                navController.navigate(Routes.Users.VERIFY_ACCOUNT.buildRoute()) {
                                    launchSingleTop = true
                                }
                            }
                            !navController.popBackStack(Routes.Dashboard.toString(), false) -> {
                                navController.navigateUp()
                            }
                        }
                    },
                ),
            )
        }
        composable(route = Routes.Users.VERIFY_ACCOUNT) {
            VerificationScreen(
                modifier = Modifier.fillMaxSize(),
                config = VerificationScreen.Config(
                    shared = shared,
                    onVerificationSuccess = {
                        if (!navController.popBackStack(Routes.Dashboard.toString(), false)) {
                            navController.navigateUp()
                        }
                    },
                ),
            )
        }
        composable(route = Routes.Users.REQUEST_PASSWORD_RESET) {
            PasswordResetRequestScreen(
                modifier = Modifier.fillMaxSize(),
                config = PasswordResetRequestScreen.Config(
                    shared = shared,
                    onPasswordResetRequestSuccess = {
                        navController.navigate(Routes.Users.RESET_PASSWORD.buildRoute()) {
                            launchSingleTop = true
                        }
                    },
                ),
            )
        }
        composable(
            route = Routes.Users.RESET_PASSWORD,
            arguments = listOf(
                navArgument("isChange") {
                    defaultValue = false.toString()
                }
            ),
        ) {
            PasswordResetScreen(
                modifier = Modifier.fillMaxSize(),
                isChange = it.arguments?.getString("isChange")?.toBooleanStrict() ?: false,
                config = PasswordResetScreen.Config(
                    shared = shared,
                    onPasswordResetSuccess = { user ->
                        if (!user.isVerified) {
                            return@Config navController.navigate(Routes.Users.VERIFY_ACCOUNT.buildRoute()) {
                                launchSingleTop = true
                            }
                        }

                        if (!navController.popBackStack(Routes.Dashboard.toString(), false)) {
                            navController.navigateUp()
                        }
                    },
                ),
            )
        }
    }
}
