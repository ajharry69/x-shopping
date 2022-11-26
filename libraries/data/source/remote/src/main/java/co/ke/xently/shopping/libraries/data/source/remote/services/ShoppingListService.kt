package co.ke.xently.shopping.libraries.data.source.remote.services

import co.ke.xently.shopping.libraries.data.source.ShoppingListItem
import co.ke.xently.shopping.libraries.data.source.remote.models.PagedData
import retrofit2.Response
import retrofit2.http.*

interface ShoppingListService {
    @POST("shopping-list/")
    suspend fun add(@Body item: ShoppingListItem): Response<ShoppingListItem>

    @PUT("shopping-list/{id}/")
    suspend fun update(
        @Path("id") id: Long,
        @Body item: ShoppingListItem,
    ): Response<ShoppingListItem>

    @DELETE("shopping-list/{id}/")
    suspend fun remove(@Path("id") id: Long): Response<Unit>

    @GET("shopping-list/{id}/")
    suspend fun get(
        @Path("id") id: Long,
        @Header("Cache-Control")
        cacheControl: String = "only-if-cached",
    ): Response<ShoppingListItem>

    @GET("shopping-list/")
    suspend fun get(
        @Query("page")
        page: Int = 1,
        @Query("size")
        size: Int? = null,
        @Header("Cache-Control")
        cacheControl: String = "only-if-cached",
        @QueryMap
        queries: Map<String, String> = emptyMap(),
    ): Response<PagedData<ShoppingListItem>>

    @GET("shopping-list/grouped/")
    suspend fun get(
        @Query("by") groupBy: String,
        @Header("Cache-Control")
        cacheControl: String = "only-if-cached",
        @Query("page") page: Int = 1,
    ): Response<Map<String, List<ShoppingListItem>>>

    /*@GET("shopping-list/recommendations/")
    suspend fun getRecommendations(
        @Query("group") group: String,
        @Query("group_by") groupBy: String,
        @Header("Cache-Control") cacheControl: String = "only-if-cached",
    ): Response<List<Recommendation>>*/

    /*@GET("shopping-list/recommendations/")
    suspend fun getRecommendations(
        @Query("lookup_id") lookupId: String,
        @Header("Cache-Control") cacheControl: String = "only-if-cached",
    ): Response<List<Recommendation>>

    @POST("shopping-list/recommendations/")
    suspend fun getRecommendations(@Body request: RecommendationRequestResource): Response<DeferredRecommendation>*/
}