package co.ke.xently.shopping.features.customers.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.ke.xently.shopping.features.customers.repositories.ICustomerRepository
import co.ke.xently.shopping.features.utils.ListState
import co.ke.xently.shopping.features.utils.State
import co.ke.xently.shopping.libraries.data.source.Customer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
internal class CustomerListViewModel @Inject constructor(
    repository: ICustomerRepository,
) : ViewModel() {
    val listState: StateFlow<ListState<Customer>> = repository.get()
        .transform { result ->
            result.onSuccess {
                emit(ListState.Success(it))
            }.onFailure {
                emit(ListState.Error(it))
            }
        }.catch {
            emit(ListState.Error(it))
        }.onStart {
            emit(ListState.Loading)
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(stopTimeout = 5.seconds),
            ListState.Loading,
        )

    private val removeId = MutableSharedFlow<Int>()
    private val _removeState = MutableSharedFlow<State<Any>>()
    val removeState = _removeState.asSharedFlow()

    init {
        viewModelScope.launch {
            removeId.collectLatest { id ->
                repository.delete(id).transformLatest { result ->
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

    fun delete(id: Int) {
        viewModelScope.launch {
            this@CustomerListViewModel.removeId.emit(id)
        }
    }
}