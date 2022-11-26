package co.ke.xently.shopping.features.recommendation.ui.request

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.ke.xently.shopping.features.recommendation.models.RecommendationRequest
import co.ke.xently.shopping.features.recommendation.repositories.IRecommendationRepository
import co.ke.xently.shopping.features.utils.State
import co.ke.xently.shopping.libraries.data.source.ShoppingListItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
internal open class RecommendationRequestViewModel @Inject constructor(
    private val stateHandle: SavedStateHandle,
    private val repository: IRecommendationRepository,
) : ViewModel() {
    private val removedSavedShoppingList =
        stateHandle.getStateFlow(REMOVED_SAVED_SHOPPING_LIST_KEY, emptySet<ShoppingListItem>())
    val hasSavedShoppingListItemInTheRecycleBin = removedSavedShoppingList
        .map(Set<ShoppingListItem>::isNotEmpty)
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    val savedShoppingList =
        stateHandle.getStateFlow(SAVED_SHOPPING_LIST_KEY, emptyList<ShoppingListItem>())
            .combine(removedSavedShoppingList) { items, itemsToRemove ->
                items - itemsToRemove
            }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun addSavedShoppingList(item: ShoppingListItem) {
        // Restore the item if it was previously flagged as deleted.
        if (item in removedSavedShoppingList.value) {
            stateHandle[REMOVED_SAVED_SHOPPING_LIST_KEY] = removedSavedShoppingList.value - item
        } else {
            stateHandle[SAVED_SHOPPING_LIST_KEY] =
                (stateHandle.get<List<ShoppingListItem>>(SAVED_SHOPPING_LIST_KEY)
                    ?: emptyList()) + item
        }
    }

    fun removeSavedShoppingList(item: ShoppingListItem) {
        stateHandle[REMOVED_SAVED_SHOPPING_LIST_KEY] = removedSavedShoppingList.value + item
    }

    fun restoreRemovedSavedItems() {
        stateHandle[REMOVED_SAVED_SHOPPING_LIST_KEY] = emptySet<ShoppingListItem>()
    }

    private fun clearSavedShoppingList() {
        stateHandle.remove<List<String>>(SAVED_SHOPPING_LIST_KEY)
        stateHandle.remove<List<String>>(REMOVED_SAVED_SHOPPING_LIST_KEY)
    }

    private val removedUnsavedShoppingList =
        stateHandle.getStateFlow(REMOVED_UNSAVED_SHOPPING_LIST_KEY, emptySet<String>())
    val hasUnsavedShoppingListItemInTheRecycleBin = removedUnsavedShoppingList
        .map(Set<String>::isNotEmpty)
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    val unsavedShoppingList =
        stateHandle.getStateFlow(UNSAVED_SHOPPING_LIST_KEY, emptyList<String>())
            .combine(removedUnsavedShoppingList) { items, itemsToRemove ->
                items - itemsToRemove
            }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun addUnsavedShoppingList(item: String) {
        // Restore the item if it was previously flagged as deleted.
        if (item in removedUnsavedShoppingList.value) {
            stateHandle[REMOVED_UNSAVED_SHOPPING_LIST_KEY] = removedUnsavedShoppingList.value - item
        } else {
            stateHandle[UNSAVED_SHOPPING_LIST_KEY] =
                (stateHandle.get<List<String>>(UNSAVED_SHOPPING_LIST_KEY) ?: emptyList()) + item
        }
    }

    fun removeUnsavedShoppingList(item: String) {
        stateHandle[REMOVED_UNSAVED_SHOPPING_LIST_KEY] = removedUnsavedShoppingList.value + item
    }

    fun restoreRemovedUnsavedItems() {
        stateHandle[REMOVED_UNSAVED_SHOPPING_LIST_KEY] = emptySet<String>()
    }

    private fun clearUnsavedShoppingList() {
        stateHandle.remove<List<String>>(UNSAVED_SHOPPING_LIST_KEY)
        stateHandle.remove<List<String>>(REMOVED_UNSAVED_SHOPPING_LIST_KEY)
    }

    fun clean() {
        clearSavedShoppingList()
        clearUnsavedShoppingList()
    }

    private val _recommendations = MutableStateFlow<State<*>>(State.Success(null))
    val recommendations = _recommendations.asStateFlow()

    private val getRecommendations = MutableSharedFlow<Boolean>()

    init {
        viewModelScope.launch {
            getRecommendations.collectLatest {
                combineTransform(savedShoppingList, unsavedShoppingList) { saved, unsaved ->
                    val items = saved + unsaved
                    Timber.i("Saved items: $saved")  // TODO: Delete..
                    Timber.i("Unsaved items: $unsaved")  // TODO: Delete..
                    Timber.i("Items: $items")  // TODO: Delete..
//                    emitAll(channelFlow { send(State.Success(listOf(Recommendation.DEFAULT))) })
                    if (items.isNotEmpty()) {
                        val flow = channelFlow {
                            val flowContext = currentCoroutineContext()
//                            coroutineScope {
                                launch(flowContext) {
                                    repository.get(RecommendationRequest(items=items))
                                        .onStart {
                                            emit(State.Loading)
                                        }
                                        .transformLatest { result ->
                                            result.onSuccess {
                                                emit(State.Success(it))
                                            }.onFailure {
                                                emit(State.Error(it))
                                            }
                                        }.collect { send(it) }
//                                }
                            }
                        }
                        /*val flow = repository.get(RecommendationRequest(items = items))
                            .onStart {
                                emit(State.Loading)
                            }
                            .transformLatest { result ->
                                result.onSuccess {
                                    emit(State.Success(it))
                                }.onFailure {
                                    emit(State.Error(it))
                                }
                            }*/
                        emitAll(flow)
                    }
                }.collect(_recommendations::emit)
            }
        }
    }

    fun getRecommendation() {
        viewModelScope.launch {
            getRecommendations.emit(Random.nextBoolean())
        }
    }

    private companion object {
        val SAVED_SHOPPING_LIST_KEY =
            RecommendationRequestViewModel::class.java.name.plus("SAVED_SHOPPING_LIST")
        val UNSAVED_SHOPPING_LIST_KEY =
            RecommendationRequestViewModel::class.java.name.plus("UNSAVED_SHOPPING_LIST")
        val REMOVED_SAVED_SHOPPING_LIST_KEY =
            RecommendationRequestViewModel::class.java.name.plus("REMOVED_SAVED_SHOPPING_LIST")
        val REMOVED_UNSAVED_SHOPPING_LIST_KEY =
            RecommendationRequestViewModel::class.java.name.plus("REMOVED_UNSAVED_SHOPPING_LIST")
    }
}