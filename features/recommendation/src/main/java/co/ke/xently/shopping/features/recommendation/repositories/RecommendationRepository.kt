package co.ke.xently.shopping.features.recommendation.repositories

import co.ke.xently.shopping.features.Dependencies
import co.ke.xently.shopping.features.recommendation.models.RecommendationRequest
import co.ke.xently.shopping.features.recommendation.models.mappers.asResource
import co.ke.xently.shopping.features.recommendation.models.mappers.asUi
import co.ke.xently.shopping.libraries.data.source.remote.ExceptionUtils.retryCatch
import co.ke.xently.shopping.libraries.data.source.remote.Http.sendRequest
import co.ke.xently.shopping.libraries.data.source.utils.Retry
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class RecommendationRepository @Inject constructor(
    private val dependencies: Dependencies,
) : IRecommendationRepository {
    override fun get(lookupId: String) = Retry().run {
        flow {
            emit(sendRequest {
                dependencies.service.recommendation.get(lookupId)
            })
        }.retryCatch(this).flowOn(dependencies.dispatcher.io)
            .map { result ->
                result.map { resources ->
                    resources.map { it.asUi }
                }
            }
    }

    override fun get(request: RecommendationRequest) = Retry().run {
        flow {
            emit(sendRequest {
                dependencies.service.recommendation.get(request.asResource)
            })
        }.retryCatch(this@run).flowOn(dependencies.dispatcher.io)
            .map { result ->
                result.map {
                    it.asUi
                }
            }
    }
}