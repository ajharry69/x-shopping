package co.ke.xently.shopping.libraries.data.source.remote.services

import co.ke.xently.shopping.libraries.data.source.remote.ProductResource
import co.ke.xently.shopping.libraries.data.source.remote.models.PagedData
import retrofit2.Response
import retrofit2.http.*

interface ProductService {
    @POST("products/")
    suspend fun add(@Body product: ProductResource): Response<ProductResource>

    @PUT("products/{id}/")
    suspend fun update(
        @Path("id") id: Long,
        @Body product: ProductResource,
    ): Response<ProductResource>

    @DELETE("products/{id}/")
    suspend fun remove(@Path("id") id: Long): Response<Unit>

    @GET("products/{id}/")
    suspend fun get(
        @Path("id") id: Long,
        @Header("Cache-Control")
        cacheControl: String = "only-if-cached",
    ): Response<ProductResource>

    @GET("products/")
    suspend fun get(
        @Query("page")
        page: Int = 1,
        @Query("size")
        size: Int? = null,
        @Header("Cache-Control")
        cacheControl: String = "only-if-cached",
        @QueryMap
        queries: Map<String, String> = emptyMap(),
    ): Response<PagedData<ProductResource>>

    @GET("search/products/")
    suspend fun get(
        @Query("query")
        query: String,
        @Query("page")
        page: Int = 1,
        @Query("size")
        size: Int? = null,
        @Header("Cache-Control")
        cacheControl: String = "only-if-cached",
        @QueryMap
        queries: Map<String, String> = emptyMap(),
    ): Response<PagedData<ProductResource>>
}