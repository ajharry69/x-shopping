package co.ke.xently.shopping.features.recommendation.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import co.ke.xently.shopping.features.recommendation.models.Recommendation
import co.ke.xently.shopping.features.ui.XentlyPreview
import co.ke.xently.shopping.features.ui.theme.XentlyTheme

@XentlyPreview
@Composable
private fun RecommendationCardItemPreview() {
    XentlyTheme {
        RecommendationCardItem(
            modifier = Modifier,
            recommendation = Recommendation.DEFAULT,
            menuItems = setOf(),
        ){

        }
    }
}