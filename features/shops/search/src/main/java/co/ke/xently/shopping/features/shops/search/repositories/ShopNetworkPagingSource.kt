package co.ke.xently.shopping.features.shops.search.repositories

import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.room.withTransaction
import co.ke.xently.shopping.features.Dependencies
import co.ke.xently.shopping.libraries.data.source.Shop
import co.ke.xently.shopping.libraries.data.source.remote.CacheControl
import co.ke.xently.shopping.libraries.data.source.remote.Http.sendRequest
import timber.log.Timber

internal class ShopNetworkPagingSource(
    private val query: String,
    private val dependencies: Dependencies,
) : PagingSource<Int, Shop>() {
    override fun getRefreshKey(state: PagingState<Int, Shop>): Int? = null

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Shop> {
        return try {
            // Start refresh at page 1 if undefined.
            val nextPageNumber = params.key ?: 1

            val data = sendRequest {
                dependencies.service.shop.get(
                    query = query,
                    page = nextPageNumber,
                    size = params.loadSize,
                    cacheControl = if (params.key == null) {
                        CacheControl.NoCache
                    } else {
                        CacheControl.OnlyIfCached
                    }.toString(),
                )
            }.map { pagedData ->
                pagedData.results.also { shopList ->
                    dependencies.database.withTransaction {
                        dependencies.database.shopDao.save(shopList.map { it.asEntity })
                    }
                }
            }.getOrDefault(emptyList())

            LoadResult.Page(
                data = data,
                prevKey = null, // Only paging forward.
                nextKey = null,
            )
        } catch (ex: Exception) {
            Timber.e(ex)
            LoadResult.Error(ex)
        }
    }
}