package co.ke.xently.shopping.features.shoppinglist.repositories

import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.room.withTransaction
import co.ke.xently.shopping.features.Dependencies
import co.ke.xently.shopping.features.shoppinglist.GroupBy
import co.ke.xently.shopping.libraries.data.source.GroupedShoppingList
import co.ke.xently.shopping.libraries.data.source.remote.CacheControl
import co.ke.xently.shopping.libraries.data.source.remote.Http.sendRequest
import timber.log.Timber

internal class GroupedShoppingListPagingSource(
    private val groupBy: GroupBy,
    private val dependencies: Dependencies,
) : PagingSource<Int, GroupedShoppingList>() {
    override fun getRefreshKey(state: PagingState<Int, GroupedShoppingList>): Int? = null

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, GroupedShoppingList> {
        params.key
        return try {
            // Start refresh at page 1 if undefined.
            val nextPageNumber = params.key ?: 1

            if (params.key == null) {
                dependencies.cache.evictAll()
            }

            val data = sendRequest {
                dependencies.service.shoppingList.get(
                    groupBy.name.lowercase(),
                    page = nextPageNumber,
                    cacheControl = if (params.key == null) {
                        CacheControl.NoCache
                    } else {
                        CacheControl.OnlyIfCached
                    }.toString(),
                )
            }.map {
                dependencies.database.withTransaction {
                    if (params.key == null) {
                        // Signifies refresh
                        dependencies.database.shoppingListDao.deleteAll()
                    }
                    it.map { entry ->
                        entry.value.saveLocally(dependencies)
                        GroupedShoppingList(
                            group = entry.key,
                            shoppingList = entry.value,
                            numberOfItems = entry.value.size,
                        )
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