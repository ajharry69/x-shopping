package co.ke.xently.shopping.ui.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import co.ke.xently.shopping.R
import co.ke.xently.shopping.libraries.data.source.User
import co.ke.xently.shopping.features.R as FeatureR

object DashboardScreen {
    data class Config(
        val onVerifyAccountRequested: () -> Unit = {},
        val onPasswordResetContinuationRequested: () -> Unit = {},
        val onPasswordResetContinuationDismissed: () -> Unit = {},
    )

    data class Item(
        val title: String,
        val logo: ImageVector,
        val enabled: Boolean = true,
        val onClick: () -> Unit = {},
    ) {
        @Composable
        operator fun invoke(modifier: Modifier) {
            val imageTint by remember(enabled) {
                derivedStateOf {
                    if (enabled) {
                        1f
                    } else {
                        0.4f
                    }
                }
            }
            ElevatedCard(
                modifier = modifier.semantics {
                    testTag = title
                },
                onClick = onClick,
                enabled = enabled,
            ) {
                Image(
                    alpha = imageTint,
                    imageVector = logo,
                    contentDescription = title,
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.CenterHorizontally)
                        .fillMaxWidth(),
                )
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    text = title,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium,
                )
            }

        }
    }

    @Composable
    operator fun invoke(
        modifier: Modifier,
        user: User?,
        items: List<Item>,
        showProgressbar: Boolean,
        snackbarHostState: SnackbarHostState?,
        config: Config,
    ) {
        val hostState = snackbarHostState ?: remember {
            SnackbarHostState()
        }

        val context = LocalContext.current

        LaunchedEffect(user) {
            if (user == null) {
                return@LaunchedEffect
            }
            if (!user.isVerified) {
                val result = hostState.showSnackbar(
                    duration = SnackbarDuration.Indefinite,
                    actionLabel = context.getString(R.string.verify).uppercase(),
                    message = context.getString(R.string.account_not_verified_verify),
                )
                if (result == SnackbarResult.ActionPerformed) {
                    config.onVerifyAccountRequested()
                }
            } else if (user.isPasswordResetRequested) {
                val result = hostState.showSnackbar(
                    withDismissAction = true,
                    duration = SnackbarDuration.Indefinite,
                    message = context.getString(R.string.pending_password_reset_request),
                    actionLabel = context.getString(R.string.continue_password_reset).uppercase(),
                )
                when (result) {
                    SnackbarResult.ActionPerformed -> {
                        config.onPasswordResetContinuationRequested()
                    }
                    SnackbarResult.Dismissed -> {
                        config.onPasswordResetContinuationDismissed()
                    }
                }
            }
        }

        val userName: String = remember(user) {
            user?.name?.ifBlank { null }
                ?: user?.email?.ifBlank { null }
                ?: "Anonymous"
        }

        Scaffold(snackbarHost = { SnackbarHost(hostState = hostState) }) { values: PaddingValues ->
            Column(modifier = modifier.padding(values)) {
                val colorPrimary = MaterialTheme.colorScheme.primary
                Header(
                    userName = userName,
                    showProgressbar = showProgressbar,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colorPrimary),
                )
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(128.dp),
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    items(items) { item ->
                        item(
                            modifier = Modifier
                                .padding(8.dp)
                                .size(128.dp),
                        )
                    }
                }
            }
        }
    }

    @Composable
    internal fun Header(modifier: Modifier, userName: String, showProgressbar: Boolean) {
        Column(modifier = modifier) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Icon(
                    modifier = Modifier.size(48.dp),
                    imageVector = Icons.Default.PointOfSale,
                    contentDescription = stringResource(R.string.app_logo),
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        fontWeight = FontWeight.Bold,
                        text = stringResource(FeatureR.string.app_name),
                        style = MaterialTheme.typography.headlineLarge,
                    )
                    Text(
                        fontWeight = FontWeight.Bold,
                        text = stringResource(R.string.app_tagline),
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    modifier = Modifier.size(100.dp),
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = stringResource(R.string.content_description_dashboard_user_profile_picture),
                )
                Text(text = userName)
                Spacer(modifier = Modifier.size(8.dp))
            }
            if (showProgressbar) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}