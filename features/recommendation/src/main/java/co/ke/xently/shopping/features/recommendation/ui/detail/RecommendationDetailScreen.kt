package co.ke.xently.shopping.features.recommendation.ui.detail

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import co.ke.xently.shopping.features.recommendation.R
import co.ke.xently.shopping.features.recommendation.models.Recommendation
import co.ke.xently.shopping.features.ui.rememberCurrencyFormat

internal object RecommendationDetailScreen {
    @Composable
    operator fun invoke(modifier: Modifier, recommendation: Recommendation) {
        val resources = LocalContext.current.resources
        val currencyFormat = rememberCurrencyFormat()
        LazyColumn(modifier = modifier) {
            if (recommendation.hit.count > 0) {
                item {
                    ListItem(
                        headlineText = {
                            Text(
                                text = resources.getQuantityString(
                                    R.plurals.recommendations_heading_hits,
                                    recommendation.hit.count,
                                ),
                                style = MaterialTheme.typography.headlineMedium,
                            )
                        },
                    )
                }
                items(recommendation.hit.items, key = { it.requested }) { hit ->
                    ListItem(
                        headlineText = {
                            Text(hit.requested)
                        },
                        supportingText = {
                            Text(hit.found)
                        },
                        trailingContent = {
                            Text(hit.unitPrice.let(currencyFormat::format))
                        },
                    )
                }
            }
            if (recommendation.miss.count > 0) {
                item {
                    ListItem(
                        headlineText = {
                            Text(
                                text = resources.getQuantityString(
                                    R.plurals.recommendations_heading_misses,
                                    recommendation.miss.count,
                                ),
                                style = MaterialTheme.typography.headlineMedium,
                            )
                        },
                    )
                }
                items(recommendation.miss.items) { miss ->
                    ListItem(
                        headlineText = {
                            Text(text = miss)
                        },
                    )
                }
            }
        }
    }
}