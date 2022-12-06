package co.ke.xently.shopping.features.recommendation.ui.request

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.ke.xently.shopping.features.recommendation.repositories.IRecommendationRepository
import co.ke.xently.shopping.libraries.data.source.ShoppingListItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal open class RecommendationRequestViewModel @Inject constructor(
    private val repository: IRecommendationRepository,
) : ViewModel() {
    val hasSavedShoppingListItemInTheRecycleBin = repository.hasSavedShoppingListItemInTheRecycleBin
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    val savedShoppingList =
        repository.savedShoppingList.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val hasUnsavedShoppingListItemInTheRecycleBin =
        repository.hasUnsavedShoppingListItemInTheRecycleBin
            .stateIn(viewModelScope, SharingStarted.Lazily, false)

    val unsavedShoppingList =
        repository.unsavedShoppingList.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val itemToBeAdded = MutableSharedFlow<String>()
    private val _itemToBeAddedExistsInUnsavedShoppingList = MutableSharedFlow<Boolean>()
    val itemToBeAddedExistsInUnsavedShoppingList =
        _itemToBeAddedExistsInUnsavedShoppingList.asSharedFlow()

    init {
        viewModelScope.launch {
            itemToBeAdded.collectLatest { item ->
                unsavedShoppingList.transformLatest {
                    emit(item.lowercase() in it.map(String::lowercase))
                }.collectLatest(_itemToBeAddedExistsInUnsavedShoppingList::emit)
            }
        }
    }

    fun lookupItemToBeAdded(item: String) {
        viewModelScope.launch {
            delay(100)
            itemToBeAdded.emit(item)
        }
    }

    fun addSavedShoppingList(item: ShoppingListItem) {
        repository.addSavedShoppingListItem(item)
    }

    fun removeSavedShoppingList(item: ShoppingListItem) {
        repository.removeSavedShoppingListItem(item)
    }

    fun restoreRemovedSavedItems() {
        repository.restoreRemovedSavedItems()
    }

    fun addUnsavedShoppingList(item: String) {
        repository.addUnsavedShoppingListItem(item)
    }

    fun removeUnsavedShoppingList(item: String) {
        repository.removeUnsavedShoppingListItem(item)
    }

    fun restoreRemovedUnsavedItems() {
        repository.restoreRemovedUnsavedItems()
    }

    fun clean() {
        repository.clearSavedShoppingList()
        repository.clearUnsavedShoppingList()
    }
}