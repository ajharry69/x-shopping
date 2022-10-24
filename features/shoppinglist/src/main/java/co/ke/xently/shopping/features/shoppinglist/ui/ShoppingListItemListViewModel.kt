package co.ke.xently.shopping.features.shoppinglist.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import co.ke.xently.shopping.features.shoppinglist.repositories.IShoppingListRepository
import co.ke.xently.shopping.features.shoppinglist.repositories.ShoppingListGroup
import co.ke.xently.shopping.features.utils.State
import co.ke.xently.shopping.libraries.data.source.ShoppingListItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class ShoppingListItemListViewModel @Inject constructor(
    repository: IShoppingListRepository,
) : ViewModel() {
    data class ShoppingListRequest(
        val config: PagingConfig = PagingConfig(30),
        val group: ShoppingListGroup? = null,
    )

    private val shoppingListRequest = MutableSharedFlow<ShoppingListRequest>()
    private val _listState = MutableStateFlow<PagingData<ShoppingListItem>>(PagingData.empty())
    val listState = _listState.asStateFlow()

    init {
        viewModelScope.launch {
            shoppingListRequest.collectLatest {
                repository.get(it.config, it.group)
                    .collectLatest(_listState::emit)
            }
        }
    }

    fun makeShoppingListRequest(request: ShoppingListRequest) {
        viewModelScope.launch {
            shoppingListRequest.emit(request)
        }
    }

    private val removeId = MutableSharedFlow<Long>()
    private val _removeState = MutableSharedFlow<State<Any>>()
    val removeState = _removeState.asSharedFlow()

    init {
        viewModelScope.launch {
            removeId.collectLatest { id ->
                repository.remove(id).transformLatest { result ->
                    result.onSuccess {
                        emit(State.Success(id))
                    }.onFailure {
                        emit(State.Error(it))
                    }
                }.onStart {
                    emit(State.Loading)
                }.catch {
                    emit(State.Error(it))
                }.collectLatest(_removeState::emit)
            }
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch {
            this@ShoppingListItemListViewModel.removeId.emit(id)
        }
    }
}