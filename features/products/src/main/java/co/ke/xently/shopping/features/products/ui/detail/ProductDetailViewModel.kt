package co.ke.xently.shopping.features.products.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.ke.xently.shopping.features.products.repositories.IProductRepository
import co.ke.xently.shopping.features.utils.State
import co.ke.xently.shopping.features.products.models.Product
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class ProductDetailViewModel @Inject constructor(
    private val repository: IProductRepository,
) : ViewModel() {
    private val product = MutableSharedFlow<Product>()
    private val _saveState = MutableSharedFlow<State<String>>()
    val saveState = _saveState.asSharedFlow()

    init {
        viewModelScope.launch {
            product.collectLatest { product ->
                repository.save(product).transformLatest { result ->
                    result.onSuccess {
                        emit(State.Success(product.toString()))
                    }.onFailure {
                        emit(State.Error(it))
                    }
                }.onStart {
                    emit(State.Loading)
                }.catch {
                    emit(State.Error(it))
                }.collectLatest(_saveState::emit)
            }
        }
    }

    fun save(product: Product) {
        viewModelScope.launch {
            this@ProductDetailViewModel.product.emit(product)
        }
    }

    private val shopId = MutableSharedFlow<Long>()
    fun get(id: Long) {
        viewModelScope.launch {
            this@ProductDetailViewModel.shopId.emit(id)
        }
    }

    val detailState: StateFlow<State<Product>> = shopId.transformLatest { id ->
        if (id == Product.DEFAULT_INSTANCE.id) {
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