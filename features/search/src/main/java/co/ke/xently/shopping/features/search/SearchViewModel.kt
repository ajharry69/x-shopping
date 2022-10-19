package co.ke.xently.shopping.features.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.ke.xently.shopping.features.search.repositories.ISearchRepository
import co.ke.xently.shopping.features.utils.ListState
import co.ke.xently.shopping.features.utils.Query
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

abstract class SearchViewModel<T> constructor(
    private val repository: ISearchRepository<T>,
) : ViewModel() {
    private val query = MutableSharedFlow<Query>()
    fun search(query: Query) {
        viewModelScope.launch {
            this@SearchViewModel.query.emit(query)
        }
    }

    fun search(query: String) {
        search(Query(query))
    }

    private val _searchState = MutableSharedFlow<ListState<T>>()
    val searchState = _searchState.asSharedFlow()

    val searchResults = searchState.mapLatest {
        (it as? ListState.Success)?.data.orEmpty()
    }.shareIn(viewModelScope, SharingStarted.WhileSubscribed(replayExpiration = 5.seconds))

    init {
        viewModelScope.launch {
            query.collectLatest { query ->
                repository.get(query = query).transformLatest { result ->
                    result.onSuccess {
                        emit(ListState.Success(it))
                    }.onFailure {
                        emit(ListState.Error(it))
                    }
                }.onStart {
                    emit(ListState.Loading)
                }.catch {
                    emit(ListState.Error(it))
                }.collectLatest(_searchState::emit)
            }
        }
    }

    private val autoCompleteQuery = MutableSharedFlow<Query>()
    private val _searchAutoCompleteState = MutableSharedFlow<ListState<T>>()

    @Suppress("MemberVisibilityCanBePrivate")
    val searchAutoCompleteState = _searchAutoCompleteState.asSharedFlow()

    val searchAutoCompleteResults = searchAutoCompleteState.mapLatest { state ->
        (state as? ListState.Success)?.data.orEmpty()
    }.shareIn(viewModelScope, SharingStarted.WhileSubscribed(replayExpiration = 5.seconds))

    init {
        viewModelScope.launch {
            autoCompleteQuery.collectLatest { query ->
                repository.get(query = query).transformLatest { result ->
                    result.onSuccess {
                        emit(ListState.Success(it))
                    }.onFailure {
                        emit(ListState.Error(it))
                    }
                }.onStart {
                    emit(ListState.Loading)
                }.catch {
                    emit(ListState.Error(it))
                }.collectLatest(_searchAutoCompleteState::emit)
            }
        }
    }

    fun autoCompleteSearch(query: Query) {
        viewModelScope.launch {
            this@SearchViewModel.autoCompleteQuery.emit(query)
        }
    }

    fun autoCompleteSearch(query: String) {
        autoCompleteSearch(Query(query, 10))
    }
}