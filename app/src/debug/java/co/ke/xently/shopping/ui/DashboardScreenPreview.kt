package co.ke.xently.shopping.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import co.ke.xently.shopping.R
import co.ke.xently.shopping.features.ui.XentlyPreview
import co.ke.xently.shopping.features.ui.theme.XentlyTheme
import co.ke.xently.shopping.libraries.data.source.User
import kotlinx.coroutines.launch

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
            snackbarHostState = null,
            config = DashboardScreen.Config(),
        ) {
            // TODO: replace content with actual content
            val scope = rememberCoroutineScope()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(text = if (it.isClosed) ">>> Swipe >>>" else "<<< Swipe <<<")
                Spacer(Modifier.height(20.dp))
                Button(onClick = { scope.launch { it.open() } }) {
                    Text("Click to open")
                }
            }
        }
    }
}