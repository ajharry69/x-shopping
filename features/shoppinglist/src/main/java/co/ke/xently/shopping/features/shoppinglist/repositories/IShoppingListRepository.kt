package co.ke.xently.shopping.features.shoppinglist.repositories

import androidx.paging.PagingConfig
import androidx.paging.PagingData
import co.ke.xently.shopping.features.shoppinglist.GroupBy
import co.ke.xently.shopping.libraries.data.source.GroupedShoppingList
import co.ke.xently.shopping.libraries.data.source.ShoppingListItem
import co.ke.xently.shopping.libraries.data.source.remote.CacheControl
import kotlinx.coroutines.flow.Flow

interface IShoppingListRepository {
    fun save(shoppingListItem: ShoppingListItem): Flow<Result<ShoppingListItem>>

    fun get(
        groupBy: GroupBy,
        cacheControl: CacheControl,
    ): Flow<Result<List<GroupedShoppingList>>>

    fun remove(id: Long): Flow<Result<Unit>>

    fun getCount(groupBy: GroupBy): Flow<Map<Any, Int>>

    fun get(id: Long): Flow<Result<ShoppingListItem?>>

    fun get(config: PagingConfig, group: ShoppingListGroup?): Flow<PagingData<ShoppingListItem>>
}