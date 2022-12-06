package co.ke.xently.shopping.features.recommendation.repositories

import androidx.lifecycle.SavedStateHandle
import co.ke.xently.shopping.features.Dependencies
import co.ke.xently.shopping.features.recommendation.models.RecommendationRequest
import co.ke.xently.shopping.features.recommendation.models.mappers.asResource
import co.ke.xently.shopping.features.recommendation.models.mappers.asUi
import co.ke.xently.shopping.libraries.data.source.ShoppingListItem
import co.ke.xently.shopping.libraries.data.source.remote.ExceptionUtils.retryCatch
import co.ke.xently.shopping.libraries.data.source.remote.Http.sendRequest
import co.ke.xently.shopping.libraries.data.source.utils.Retry
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class RecommendationRepository @Inject constructor(
    private val dependencies: Dependencies,
) : IRecommendationRepository {
    private val removedSavedShoppingList =
        stateHandle.getStateFlow(REMOVED_SAVED_SHOPPING_LIST_KEY, emptySet<ShoppingListItem>())

    override val hasSavedShoppingListItemInTheRecycleBin = removedSavedShoppingList
        .map(Set<ShoppingListItem>::isNotEmpty)

    override val savedShoppingList =
        stateHandle.getStateFlow(SAVED_SHOPPING_LIST_KEY, emptyList<ShoppingListItem>())
            .combine(removedSavedShoppingList) { items, itemsToRemove ->
                items - itemsToRemove
            }

    private val removedUnsavedShoppingList =
        stateHandle.getStateFlow(REMOVED_UNSAVED_SHOPPING_LIST_KEY, emptySet<String>())

    override val hasUnsavedShoppingListItemInTheRecycleBin = removedUnsavedShoppingList
        .map(Set<String>::isNotEmpty)

    override val unsavedShoppingList =
        stateHandle.getStateFlow(UNSAVED_SHOPPING_LIST_KEY, emptyList<String>())
            .combine(removedUnsavedShoppingList) { items, itemsToRemove ->
                items - itemsToRemove
            }

    override fun addSavedShoppingListItem(item: ShoppingListItem) {
        // Restore the item if it was previously flagged as deleted.
        if (item in removedSavedShoppingList.value) {
            stateHandle[REMOVED_SAVED_SHOPPING_LIST_KEY] = removedSavedShoppingList.value - item
        } else {
            stateHandle[SAVED_SHOPPING_LIST_KEY] =
                (stateHandle.get<List<ShoppingListItem>>(SAVED_SHOPPING_LIST_KEY)
                    ?: emptyList()) + item
        }
    }

    override fun removeSavedShoppingListItem(item: ShoppingListItem) {
        stateHandle[REMOVED_SAVED_SHOPPING_LIST_KEY] = removedSavedShoppingList.value + item
    }

    override fun restoreRemovedSavedItems() {
        stateHandle[REMOVED_SAVED_SHOPPING_LIST_KEY] = emptySet<ShoppingListItem>()
    }

    override fun clearSavedShoppingList() {
        stateHandle.remove<List<String>>(SAVED_SHOPPING_LIST_KEY)
        stateHandle.remove<List<String>>(REMOVED_SAVED_SHOPPING_LIST_KEY)
    }

    override fun addUnsavedShoppingListItem(item: String) {
        // Restore the item if it was previously flagged as deleted.
        val itemInLowerCase = item.lowercase()
        val itemToRestore = removedUnsavedShoppingList.value.firstOrNull {
            it.lowercase() == itemInLowerCase
        }
        if (itemToRestore != null) {
            stateHandle[REMOVED_UNSAVED_SHOPPING_LIST_KEY] =
                removedUnsavedShoppingList.value - itemToRestore
        } else {
            stateHandle[UNSAVED_SHOPPING_LIST_KEY] =
                (stateHandle.get<List<String>>(UNSAVED_SHOPPING_LIST_KEY) ?: emptyList()) + item
        }
    }

    override fun removeUnsavedShoppingListItem(item: String) {
        stateHandle[REMOVED_UNSAVED_SHOPPING_LIST_KEY] = removedUnsavedShoppingList.value + item
    }

    override fun restoreRemovedUnsavedItems() {
        stateHandle[REMOVED_UNSAVED_SHOPPING_LIST_KEY] = emptySet<String>()
    }

    override fun clearUnsavedShoppingList() {
        stateHandle.remove<List<String>>(UNSAVED_SHOPPING_LIST_KEY)
        stateHandle.remove<List<String>>(REMOVED_UNSAVED_SHOPPING_LIST_KEY)
    }

    override fun get(lookupId: String) = Retry().run {
        flow {
            emit(sendRequest {
                dependencies.service.recommendation.get(lookupId)
            })
        }.retryCatch(this).flowOn(dependencies.dispatcher.io)
            .map { result ->
                result.map { resources ->
                    resources.map { it.asUi }
                }
            }
    }

    override fun get() =
        combineTransform(savedShoppingList, unsavedShoppingList) { saved, unsaved ->
            val items = saved + unsaved
            if (items.isNotEmpty()) {
                emitAll(get(RecommendationRequest(items = items)))
            }
        }

    override fun get(request: RecommendationRequest) = Retry().run {
        flow {
            emit(sendRequest {
                dependencies.service.recommendation.get(request.asResource)
            })
        }.retryCatch(this@run).flowOn(dependencies.dispatcher.io)
            .map { result ->
                result.map {
                    it.asUi
                }
            }
    }

    private companion object {
        private val stateHandle = SavedStateHandle()
        private val SAVED_SHOPPING_LIST_KEY =
            RecommendationRepository::class.java.name.plus("SAVED_SHOPPING_LIST")
        private val UNSAVED_SHOPPING_LIST_KEY =
            RecommendationRepository::class.java.name.plus("UNSAVED_SHOPPING_LIST")
        private val REMOVED_SAVED_SHOPPING_LIST_KEY =
            RecommendationRepository::class.java.name.plus("REMOVED_SAVED_SHOPPING_LIST")
        private val REMOVED_UNSAVED_SHOPPING_LIST_KEY =
            RecommendationRepository::class.java.name.plus("REMOVED_UNSAVED_SHOPPING_LIST")
    }
}