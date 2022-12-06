package co.ke.xently.shopping.features.recommendation.repositories

import co.ke.xently.shopping.features.recommendation.models.DeferredRecommendation
import co.ke.xently.shopping.features.recommendation.models.Recommendation
import co.ke.xently.shopping.features.recommendation.models.RecommendationRequest
import co.ke.xently.shopping.libraries.data.source.ShoppingListItem
import kotlinx.coroutines.flow.Flow

internal interface IRecommendationRepository {
    val hasSavedShoppingListItemInTheRecycleBin: Flow<Boolean>

    val savedShoppingList: Flow<List<ShoppingListItem>>

    val hasUnsavedShoppingListItemInTheRecycleBin: Flow<Boolean>

    val unsavedShoppingList: Flow<List<String>>

    fun addSavedShoppingListItem(item: ShoppingListItem)

    fun removeSavedShoppingListItem(item: ShoppingListItem)

    fun restoreRemovedSavedItems()

    fun clearSavedShoppingList()

    fun addUnsavedShoppingListItem(item: String)

    fun removeUnsavedShoppingListItem(item: String)

    fun restoreRemovedUnsavedItems()

    fun clearUnsavedShoppingList()

    fun get(lookupId: String): Flow<Result<List<Recommendation>>>

    fun get(): Flow<Result<DeferredRecommendation>>

    fun get(request: RecommendationRequest): Flow<Result<DeferredRecommendation>>
}