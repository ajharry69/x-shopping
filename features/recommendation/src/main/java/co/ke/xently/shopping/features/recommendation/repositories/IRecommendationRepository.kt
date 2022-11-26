package co.ke.xently.shopping.features.recommendation.repositories

import co.ke.xently.shopping.features.recommendation.models.DeferredRecommendation
import co.ke.xently.shopping.features.recommendation.models.Recommendation
import co.ke.xently.shopping.features.recommendation.models.RecommendationRequest
import kotlinx.coroutines.flow.Flow

internal interface IRecommendationRepository {
    fun get(lookupId: String): Flow<Result<List<Recommendation>>>

    fun get(request: RecommendationRequest): Flow<Result<DeferredRecommendation>>
}