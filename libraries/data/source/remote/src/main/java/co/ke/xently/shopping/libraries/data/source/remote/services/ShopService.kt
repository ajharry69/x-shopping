package co.ke.xently.shopping.libraries.data.source.remote.services

import co.ke.xently.shopping.libraries.data.source.Shop
import co.ke.xently.shopping.libraries.data.source.remote.models.PagedData
import retrofit2.Response
import retrofit2.http.*

interface ShopService {
    @POST("shops/")
    suspend fun add(@Body shop: Shop): Response<Shop>

    @PUT("shops/{id}/")
    suspend fun update(@Path("id") id: Long, @Body shop: Shop): Response<Shop>

    @DELETE("shops/{id}/")
    suspend fun remove(@Path("id") id: Long): Response<Unit>

    @GET("shops/{id}/")
    suspend fun get(
        @Path("id") id: Long,
        @Header("Cache-Control")
        cacheControl: String = "only-if-cached",
    ): Response<Shop>

    @GET("shops/")
    suspend fun get(
        @Query("page")
        page: Int = 1,
        @Query("size")
        size: Int? = null,
        @Header("Cache-Control")
        cacheControl: String = "only-if-cached",
        @QueryMap
        queries: Map<String, String> = emptyMap(),
    ): Response<PagedData<Shop>>

    @GET("search/shops/")
    suspend fun get(
        @Query("q")
        query: String,
        @Query("page")
        page: Int = 1,
        @Query("size")
        size: Int? = null,
        @Header("Cache-Control")
        cacheControl: String = "only-if-cached",
        @QueryMap
        queries: Map<String, String> = emptyMap(),
    ): Response<PagedData<Shop>>
}