package co.ke.xently.shopping.features.customers.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.ke.xently.shopping.features.customers.R
import co.ke.xently.shopping.features.customers.repositories.ICustomerRepository
import co.ke.xently.shopping.features.utils.State
import co.ke.xently.shopping.libraries.data.source.Customer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class CustomerDetailScreenViewModel @Inject constructor(
    private val repository: ICustomerRepository,
) : ViewModel() {
    companion object {
        private val SQL_CONSTRAINT_ERROR_LOOKUPS = listOf(
            "taxPin" to R.string.fc_error_duplicate_tax_pin,
        )
    }

    private val customer = MutableSharedFlow<Customer>()
    private val _saveState = MutableSharedFlow<State<String>>()
    val saveState = _saveState.asSharedFlow()

    init {
        viewModelScope.launch {
            customer.collectLatest { customer ->
                repository.save(customer).transformLatest { result ->
                    result.onSuccess {
                        emit(State.Success(customer.toString()))
                    }.onFailure {
                        emit(State.Error(it, SQL_CONSTRAINT_ERROR_LOOKUPS))
                    }
                }.onStart {
                    emit(State.Loading)
                }.catch {
                    emit(State.Error(it, SQL_CONSTRAINT_ERROR_LOOKUPS))
                }.collectLatest(_saveState::emit)
            }
        }
    }

    fun save(customer: Customer) {
        viewModelScope.launch {
            this@CustomerDetailScreenViewModel.customer.emit(customer)
        }
    }

    private val customerId = MutableSharedFlow<Int>()
    fun get(id: Int) {
        viewModelScope.launch {
            this@CustomerDetailScreenViewModel.customerId.emit(id)
        }
    }

    val detailState: StateFlow<State<Customer>> = customerId.transformLatest { id ->
        if (id == Customer.DEFAULT_INSTANCE.id) {
            emit(State.Success(null))
        } else {
            val taxFlow = repository.get(id)
                .transform { result ->
                    result.onSuccess {
                        emit(State.Success(it))
                    }.onFailure {
                        emit(State.Error(it))
                    }
                }
            emitAll(taxFlow)
        }
    }.catch {
        emit(State.Error(it))
    }.onStart {
        emit(State.Loading)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), State.Success(null))
}