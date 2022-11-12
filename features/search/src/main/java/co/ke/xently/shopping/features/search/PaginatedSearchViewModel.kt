package co.ke.xently.shopping.features.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import co.ke.xently.shopping.features.search.repositories.IPaginatedSearchRepository
import co.ke.xently.shopping.features.utils.Query
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Suppress("MemberVisibilityCanBePrivate")
abstract class PaginatedSearchViewModel<T : Any> constructor(
    private val repository: IPaginatedSearchRepository<T>,
) : ViewModel() {
    data class Request(val query: Query, val config: PagingConfig = DEFAULT_CONFIG){
        companion object{
            val DEFAULT_CONFIG = PagingConfig(30)
        }
    }

    private val request = MutableSharedFlow<Request>()
    fun search(request: Request) {
        viewModelScope.launch {
            delay(250)
            this@PaginatedSearchViewModel.request.emit(request)
        }
    }

    private val _search = MutableSharedFlow<PagingData<T>>()
    val search = _search.asSharedFlow()

    init {
        viewModelScope.launch {
            request.collectLatest { request ->
                repository.get(config = request.config, query = request.query)
                    .cachedIn(viewModelScope)
                    .collectLatest(_search::emit)
            }
        }
    }

    private val autoCompleteRequest = MutableSharedFlow<Request>()
    private val _searchAutoComplete = MutableSharedFlow<PagingData<T>>()

    val searchAutoComplete = _searchAutoComplete.asSharedFlow()

    init {
        viewModelScope.launch {
            autoCompleteRequest.collectLatest { request ->
                repository.get(config = request.config, query = request.query)
                    .cachedIn(viewModelScope)
                    .collectLatest(_searchAutoComplete::emit)
            }
        }
    }

    fun autoCompleteSearch(request: Request) {
        viewModelScope.launch {
            delay(250)
            this@PaginatedSearchViewModel.autoCompleteRequest.emit(request)
        }
    }
}