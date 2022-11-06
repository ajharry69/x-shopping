package co.ke.xently.shopping.features.products.shared.repositories

import co.ke.xently.shopping.features.Dependencies
import co.ke.xently.shopping.features.search.repositories.ISearchRepository
import co.ke.xently.shopping.features.utils.Query
import co.ke.xently.shopping.libraries.data.source.AbstractBrand
import co.ke.xently.shopping.libraries.data.source.ShoppingListItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BrandSearchRepository @Inject constructor(private val dependencies: Dependencies) :
    ISearchRepository<AbstractBrand> {
    override fun get(query: Query): Flow<Result<List<AbstractBrand>>> {
        return if (query.filters["uniqueByName"] == true) {
            dependencies.database.shoppingListDao.getBrandsUniqueByName(query = "%${query.value}%")
        } else {
            dependencies.database.shoppingListDao.getBrands(query = "%${query.value}%")
        }.flowOn(dependencies.dispatcher.io).map { entityBrands ->
            entityBrands.map {
                ShoppingListItem.Brand(name = it.name)
            }.take(query.size)
        }.map {
            Result.success(it)
        }
    }
}