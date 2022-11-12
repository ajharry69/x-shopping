package co.ke.xently.shopping.features.shoppinglist.repositories

import co.ke.xently.shopping.features.Dependencies
import co.ke.xently.shopping.libraries.data.source.ShoppingListItem
import co.ke.xently.shopping.libraries.data.source.local.ShoppingListItemEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal suspend fun List<ShoppingListItem>.saveLocally(
    dependencies: Dependencies,
    scope: CoroutineScope = CoroutineScope(dependencies.dispatcher.io),
) {
    scope.launch(dependencies.dispatcher.io) {
        val items = this@saveLocally.map { it.asEntity }
        dependencies.database.shoppingListDao.save(items)
    }
    val brands = withContext(dependencies.dispatcher.computation) {
        flatMap { item ->
            item.brands.map {
                ShoppingListItemEntity.Brand(name = it.name, shoppingListItemId = item.id)
            }
        }
    }
    scope.launch(dependencies.dispatcher.io) {
        dependencies.database.shoppingListDao.saveBrands(brands)
    }
    val attributes = withContext(dependencies.dispatcher.computation) {
        flatMap { item ->
            item.attributes.flatMap { attr ->
                (attr.values ?: emptyList()).mapTo(mutableListOf(attr)) {
                    attr.copy(value = it)
                }
            }.map {
                ShoppingListItemEntity.Attribute(
                    name = it.name,
                    value = it.value,
                    shoppingListItemId = item.id,
                )
            }
        }
    }
    scope.launch(dependencies.dispatcher.io) {
        dependencies.database.shoppingListDao.saveAttributes(attributes)
    }
}

internal suspend fun ShoppingListItem.saveLocally(
    dependencies: Dependencies,
    scope: CoroutineScope = CoroutineScope(dependencies.dispatcher.io),
) {
    listOf(this).saveLocally(dependencies, scope)
}