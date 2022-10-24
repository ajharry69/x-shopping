package co.ke.xently.shopping.features.shoppinglist.repositories

import co.ke.xently.shopping.features.Dependencies
import co.ke.xently.shopping.features.search.repositories.ISearchRepository
import co.ke.xently.shopping.features.utils.Query
import co.ke.xently.shopping.libraries.data.source.ShoppingListItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShoppingListItemSearchRepository @Inject constructor(
    private val dependencies: Dependencies,
) : ISearchRepository<ShoppingListItem> {
    /*override fun get(query: Query) = dependencies.database.shoppingListDao.run {
        if (query.value.isBlank()) {
            get()
        } else {
            get("%${query}%")
        }
    }.flowOn(dependencies.dispatcher.io).map { entities ->
        val instances = entities.run {
            if (query.size < 0) {
                this
            } else {
                take(query.size)
            }
        }.map {
            it.asUIInstance
        }
        Result.success(instances)
    }.flowOn(dependencies.dispatcher.computation)*/
    override fun get(query: Query): Flow<Result<List<ShoppingListItem>>> {
        TODO("Not yet implemented")
    }
}