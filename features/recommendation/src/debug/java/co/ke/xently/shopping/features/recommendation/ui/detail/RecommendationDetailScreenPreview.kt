package co.ke.xently.shopping.features.recommendation.ui.detail

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import co.ke.xently.shopping.features.recommendation.models.Recommendation
import co.ke.xently.shopping.features.ui.XentlyPreview
import co.ke.xently.shopping.features.ui.theme.XentlyTheme

@XentlyPreview
@Composable
private fun RecommendationDetailScreenPreview() {
    XentlyTheme {
        RecommendationDetailScreen(
            modifier = Modifier.fillMaxSize(),
            recommendation = Recommendation.DEFAULT,
        )
    }
}