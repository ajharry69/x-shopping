package co.ke.xently.shopping.features.products.repositories

import co.ke.xently.shopping.features.Dependencies
import co.ke.xently.shopping.features.products.models.mappers.asEntity
import co.ke.xently.shopping.libraries.data.source.local.models.ProductEntity
import co.ke.xently.shopping.libraries.data.source.remote.models.ProductResource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal suspend fun List<ProductResource>.saveLocally(
    dependencies: Dependencies,
    scope: CoroutineScope = CoroutineScope(dependencies.dispatcher.io),
) {
    scope.launch(dependencies.dispatcher.io) {
        val items = this@saveLocally.map { it.asEntity }
        dependencies.database.productDao.save(items)
    }
    val brands = withContext(dependencies.dispatcher.computation) {
        flatMap { item ->
            item.brands.map {
                ProductEntity.Brand(name = it.name, productId = item.id)
            }
        }
    }
    scope.launch(dependencies.dispatcher.io) {
        dependencies.database.productDao.saveBrands(brands)
    }
    val attributes = withContext(dependencies.dispatcher.computation) {
        flatMap { item ->
            item.attributes.flatMap { attr ->
                (attr.values ?: emptyList()).mapTo(mutableListOf(attr)) {
                    attr.copy(value = it)
                }
            }.map {
                ProductEntity.Attribute(
                    name = it.name,
                    value = it.value,
                    productId = item.id,
                )
            }
        }
    }
    scope.launch(dependencies.dispatcher.io) {
        dependencies.database.productDao.saveAttributes(attributes)
    }
}

internal suspend fun ProductResource.saveLocally(
    dependencies: Dependencies,
    scope: CoroutineScope = CoroutineScope(dependencies.dispatcher.io),
) {
    listOf(this).saveLocally(dependencies, scope)
}