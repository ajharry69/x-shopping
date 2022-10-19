package co.ke.xently.shopping.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import co.ke.xently.shopping.R
import co.ke.xently.shopping.features.ui.XentlyPreview
import co.ke.xently.shopping.features.ui.theme.XentlyTheme
import co.ke.xently.shopping.libraries.data.source.User

@XentlyPreview
@Composable
private fun DashboardScreenHeaderPreview() {
    XentlyTheme {
        DashboardScreen.Header(
            userName = "John Doe",
            showProgressbar = true,
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary),
        )
    }
}

@XentlyPreview
@Composable
private fun DashboardScreenPreview() {
    XentlyTheme {
        DashboardScreen(
            modifier = Modifier.fillMaxSize(),
            user = User.DEFAULT_INSTANCE.copy(name = "John Doe"),
            items = listOf(
                DashboardScreen.Item(
                    enabled = false,
                    logo = Icons.Default.Widgets,
                    title = stringResource(R.string.dashboard_item_products),
                ),
                DashboardScreen.Item(
                    logo = Icons.Default.ShoppingCart,
                    title = stringResource(R.string.dashboard_item_purchase),
                ),
                DashboardScreen.Item(
                    logo = Icons.Default.ShoppingBasket,
                    title = stringResource(R.string.dashboard_item_sales),
                ),
                DashboardScreen.Item(
                    logo = Icons.Default.Groups,
                    title = stringResource(R.string.dashboard_item_customers),
                ),
                DashboardScreen.Item(
                    logo = Icons.Default.Assessment,
                    title = stringResource(R.string.dashboard_item_reports),
                ),
                DashboardScreen.Item(
                    logo = Icons.Default.AccountBalance,
                    title = stringResource(R.string.dashboard_item_taxes),
                ),
                DashboardScreen.Item(
                    logo = Icons.Default.QrCode,
                    title = stringResource(R.string.dashboard_item_hs_codes),
                ),
                DashboardScreen.Item(
                    logo = Icons.Default.Settings,
                    title = stringResource(R.string.dashboard_item_settings),
                ),
                DashboardScreen.Item(
                    logo = Icons.Default.Logout,
                    title = stringResource(R.string.dashboard_item_logout),
                ),
            ),
            showProgressbar = false,
            snackbarHostState = null,
            config = DashboardScreen.Config(),
        )
    }
}