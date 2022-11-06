package co.ke.xently.shopping.features.shops.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.ke.xently.shopping.features.shops.repositories.IShopRepository
import co.ke.xently.shopping.features.utils.State
import co.ke.xently.shopping.libraries.data.source.Shop
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class ShopDetailViewModel @Inject constructor(
    private val repository: IShopRepository,
) : ViewModel() {
    private val shop = MutableSharedFlow<Shop>()
    private val _saveState = MutableSharedFlow<State<String>>()
    val saveState = _saveState.asSharedFlow()

    init {
        viewModelScope.launch {
            shop.collectLatest { shop ->
                repository.save(shop).transformLatest { result ->
                    result.onSuccess {
                        emit(State.Success(shop.toString()))
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

    fun save(shop: Shop) {
        viewModelScope.launch {
            this@ShopDetailViewModel.shop.emit(shop)
        }
    }

    private val shopId = MutableSharedFlow<Long>()
    fun get(id: Long) {
        viewModelScope.launch {
            this@ShopDetailViewModel.shopId.emit(id)
        }
    }

    val detailState: StateFlow<State<Shop>> = shopId.transformLatest { id ->
        if (id == Shop.DEFAULT_INSTANCE.id) {
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