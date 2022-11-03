package co.ke.xently.shopping.features.shoppinglist.ui.detail

import android.icu.util.MeasureUnit
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.ke.xently.shopping.features.shoppinglist.R
import co.ke.xently.shopping.features.shoppinglist.repositories.IShoppingListRepository
import co.ke.xently.shopping.features.utils.State
import co.ke.xently.shopping.libraries.common.Dispatcher
import co.ke.xently.shopping.libraries.data.source.AbstractAttribute
import co.ke.xently.shopping.libraries.data.source.AbstractBrand
import co.ke.xently.shopping.libraries.data.source.ShoppingListItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class ShoppingListItemDetailScreenViewModel @Inject constructor(
    private val dispatcher: Dispatcher,
    private val repository: IShoppingListRepository,
) : ViewModel() {
    companion object {
        private val SQL_CONSTRAINT_ERROR_LOOKUPS = listOf(
            "taxPin" to R.string.feature_shoppinglist_error_duplicate_tax_pin,
        )
    }

    private val shoppingListItem = MutableSharedFlow<ShoppingListItem>()
    private val _saveState = MutableSharedFlow<State<String>>()
    val saveState = _saveState.asSharedFlow()

    init {
        viewModelScope.launch {
            shoppingListItem.collectLatest { shoppingListItem ->
                repository.save(shoppingListItem).transformLatest { result ->
                    result.onSuccess {
                        emit(State.Success(shoppingListItem.toString()))
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

    fun save(shoppingListItem: ShoppingListItem) {
        viewModelScope.launch {
            this@ShoppingListItemDetailScreenViewModel.shoppingListItem.emit(shoppingListItem)
        }
    }

    private val customerId = MutableSharedFlow<Long>()
    fun get(id: Long) {
        viewModelScope.launch {
            this@ShoppingListItemDetailScreenViewModel.customerId.emit(id)
        }
    }

    val detailState: StateFlow<State<ShoppingListItem>> = customerId.transformLatest { id ->
        if (id == ShoppingListItem.DEFAULT_INSTANCE.id) {
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

    private val measurementUnitQuery = MutableSharedFlow<String>()
    private val _measurementUnitSuggestions = MutableStateFlow(emptyList<MeasureUnit>())
    val measurementUnitSuggestions = _measurementUnitSuggestions.asStateFlow()

    init {
        viewModelScope.launch {
            measurementUnitQuery.collectLatest { query ->
                flow {
                    val units = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        MeasureUnit.getAvailable().flatMap {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                it.splitToSingleUnits()
                            } else {
                                listOf(it)
                            }
                        }.filter {
                            it.toString().contains(query, ignoreCase = true)
                        }
                    } else {
                        emptyList()
                    }
                    emit(units)
                }.flowOn(dispatcher.computation).collectLatest(_measurementUnitSuggestions::emit)
            }
        }
    }

    fun setMeasurementUnitQuery(query: String) {
        viewModelScope.launch {
            delay(500)
            measurementUnitQuery.emit(query)
        }
    }

    private val attributeQuery = MutableSharedFlow<String>()
    private val _attributeSuggestions = MutableStateFlow(emptyList<AbstractAttribute>())
    val attributeSuggestions = _attributeSuggestions.asStateFlow()

    init {
        viewModelScope.launch {
            attributeQuery.collectLatest { query ->
                flow {
                    emit(emptyList<AbstractAttribute>())
                }.flowOn(dispatcher.computation).collectLatest(_attributeSuggestions::emit)
            }
        }
    }

    fun setAttributeQuery(query: String) {
        viewModelScope.launch {
            delay(500)
            attributeQuery.emit(query)
        }
    }
}