package co.ke.xently.shopping.features.products.shared.repositories

import co.ke.xently.shopping.features.Dependencies
import co.ke.xently.shopping.features.search.repositories.ISearchRepository
import co.ke.xently.shopping.features.utils.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AttributeNameSearchRepository @Inject constructor(private val dependencies: Dependencies) :
    ISearchRepository<String> {
    override fun get(query: Query): Flow<Result<List<String>>> {
        return dependencies.database.shoppingListDao.getUniqueAttributeNames(query = "%${query.value}%")
            .flowOn(dependencies.dispatcher.io).map {
                it.take(query.size)
            }.map {
                Result.success(it)
            }
    }
}