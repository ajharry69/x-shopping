package co.ke.xently.shopping.features.products.shared.repositories

import co.ke.xently.shopping.features.Dependencies
import co.ke.xently.shopping.features.search.repositories.ISearchRepository
import co.ke.xently.shopping.features.utils.Query
import co.ke.xently.shopping.libraries.data.source.AbstractAttribute
import co.ke.xently.shopping.libraries.data.source.ShoppingListItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AttributeSearchRepository @Inject constructor(private val dependencies: Dependencies) :
    ISearchRepository<AbstractAttribute> {
    override fun get(query: Query): Flow<Result<List<AbstractAttribute>>> {
        return dependencies.database.shoppingListDao.getAttributes(
            name = query.filters["name"]!!.toString(),
            value = "%${query.value}%",
        ).flowOn(dependencies.dispatcher.io).map { entityAttributes ->
            entityAttributes.map {
                ShoppingListItem.Attribute(name = it.name, value = it.value)
            }.take(query.size)
        }.map {
            Result.success(it)
        }
    }
}