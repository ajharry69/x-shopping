package co.ke.xently.shopping.features.shoppinglist.ui.list.grouped

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import co.ke.xently.shopping.features.shoppinglist.GroupBy
import co.ke.xently.shopping.features.shoppinglist.repositories.IShoppingListRepository
import co.ke.xently.shopping.features.utils.State
import co.ke.xently.shopping.libraries.data.source.GroupedShoppingList
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GroupedShoppingListViewModel @Inject constructor(
    repository: IShoppingListRepository,
) : ViewModel() {
    data class Request(
        val config: PagingConfig = PagingConfig(30),
        val groupBy: GroupBy = GroupBy.DateAdded,
    )

    private val request = MutableSharedFlow<Request>()

    private val _groupedShoppingList =
        MutableStateFlow<PagingData<GroupedShoppingList>>(PagingData.empty())
    internal val groupedShoppingList = _groupedShoppingList.asStateFlow()

    private val _groupedShoppingListCount = MutableStateFlow<Map<Any, Int>>(emptyMap())
    internal val groupedShoppingListCount = _groupedShoppingListCount.asStateFlow()

    init {
        viewModelScope.launch {
            request.collectLatest {
                repository.get(config = it.config, groupBy = it.groupBy)
                    .cachedIn(viewModelScope)
                    .collectLatest(_groupedShoppingList::emit)
            }
        }
        viewModelScope.launch {
            request.collectLatest {
                repository.getCount(it.groupBy)
                    .collectLatest(_groupedShoppingListCount::emit)
            }
        }
    }

    internal fun fetchGroupedShoppingList(request: Request) {
        viewModelScope.launch {
            this@GroupedShoppingListViewModel.request.emit(request)
        }
    }

    private val removeId = MutableSharedFlow<Long>()
    private val _removeState = MutableSharedFlow<State<Any>>()
    internal val removeState = _removeState.asSharedFlow()

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

    internal fun delete(id: Long) {
        viewModelScope.launch {
            this@GroupedShoppingListViewModel.removeId.emit(id)
        }
    }
}