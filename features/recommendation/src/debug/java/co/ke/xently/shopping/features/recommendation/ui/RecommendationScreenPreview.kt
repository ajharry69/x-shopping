package co.ke.xently.shopping.features.recommendation.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import co.ke.xently.shopping.features.recommendation.models.Recommendation
import co.ke.xently.shopping.features.recommendation.models.RecommendationRequest
import co.ke.xently.shopping.features.ui.XentlyPreview
import co.ke.xently.shopping.features.ui.theme.XentlyTheme
import co.ke.xently.shopping.features.utils.State

@XentlyPreview
@Composable
private fun RecommendationScreenPreview() {
    XentlyTheme {
        var recommendation by remember {
            mutableStateOf<Recommendation?>(null)
        }
        RecommendationScreen(
            modifier = Modifier.fillMaxSize(),
            config = RecommendationScreen.Config().copy(
                onDetailClick = { recommendation = it },
            ),
            state = State.Success(data = listOf(Recommendation.DEFAULT)),
            recommendation = recommendation,
            request = RecommendationRequest(emptyList()),
        )
    }
}

@Preview
@Composable
private fun RecommendationScreenLoadingPreview() {
    XentlyTheme {
        RecommendationScreen(
            modifier = Modifier.fillMaxSize(),
            config = RecommendationScreen.Config(),
            request = RecommendationRequest(emptyList()),
            state = State.Loading,
            recommendation = null,
        )
    }
}

@Preview
@Composable
private fun RecommendationScreenErrorPreview() {
    XentlyTheme {
        RecommendationScreen(
            modifier = Modifier.fillMaxSize(),
            request = RecommendationRequest(emptyList()),
            config = RecommendationScreen.Config(),
            state = State.Error(RuntimeException("An error was encountered.")),
            recommendation = null,
        )
    }
}

