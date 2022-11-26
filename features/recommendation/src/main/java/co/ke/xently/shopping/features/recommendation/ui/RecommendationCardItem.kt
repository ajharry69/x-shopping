package co.ke.xently.shopping.features.recommendation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import co.ke.xently.shopping.features.models.MenuItem
import co.ke.xently.shopping.features.recommendation.R
import co.ke.xently.shopping.features.recommendation.models.Recommendation
import co.ke.xently.shopping.features.ui.ListItemTrailingIconButton
import co.ke.xently.shopping.features.ui.rememberCurrencyFormat

internal object RecommendationCardItem {
    @Composable
    operator fun invoke(
        modifier: Modifier,
        recommendation: Recommendation,
        menuItems: Set<MenuItem<Recommendation>>,
        onClick: (Recommendation) -> Unit,
    ) {
        val currencyFormat = rememberCurrencyFormat()
        ListItem(
            modifier = Modifier
                .clickable { onClick(recommendation) }
                .then(modifier),
            headlineText = {
                Text(
                    modifier = Modifier.wrapContentWidth(),
                    text = recommendation.shop.name,
                )
            },
            supportingText = {
                Text(
                    modifier = Modifier.wrapContentWidth(),
                    text = LocalContext.current.resources.getQuantityString(
                        R.plurals.recommendations_item_description,
                        recommendation.numberOfItems,
                        recommendation.hit.count,
                        recommendation.numberOfItems,
                        recommendation.expenditure.total.let(currencyFormat::format),
                    ),
                )
            },
            trailingContent = {
                ListItemTrailingIconButton(
                    data = recommendation,
                    menuItems = menuItems,
                    iconContentDescription = { recommendation.shop.name },
                )
            },
        )
    }
}