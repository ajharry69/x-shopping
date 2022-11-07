package co.ke.xently.shopping.features.products.shopsearch

import co.ke.xently.shopping.features.Dependencies
import co.ke.xently.shopping.features.search.repositories.ISearchRepository
import co.ke.xently.shopping.features.utils.Query
import co.ke.xently.shopping.libraries.data.source.Shop
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShopSearchRepository @Inject constructor(private val dependencies: Dependencies) :
    ISearchRepository<Shop> {
    override fun get(query: Query): Flow<Result<List<Shop>>> {
        return dependencies.database.shopDao.get(query = "%${query.value}%")
            .flowOn(dependencies.dispatcher.io).map { entityBrands ->
                entityBrands.map {
                    it.asUIInstance
                }.take(query.size)
            }.map {
                Result.success(it)
            }
    }
}