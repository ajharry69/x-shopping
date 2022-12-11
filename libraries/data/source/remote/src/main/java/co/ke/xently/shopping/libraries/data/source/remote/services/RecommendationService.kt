package co.ke.xently.shopping.libraries.data.source.remote.services

import co.ke.xently.shopping.libraries.data.source.remote.models.RecommendationRequestResource
import co.ke.xently.shopping.libraries.data.source.remote.models.RecommendationResource
import retrofit2.Response
import retrofit2.http.*

interface RecommendationService {
    /*@GET("shopping-list/recommendations/")
    suspend fun get(
        @Query("group") group: String,
        @Query("group_by") groupBy: String,
        @Header("Cache-Control") cacheControl: String = "only-if-cached",
    ): Response<List<RecommendationResource>>*/

    @POST("shopping-list/recommendations/")
    suspend fun get(@Body request: RecommendationRequestResource): Response<List<RecommendationResource>>
}