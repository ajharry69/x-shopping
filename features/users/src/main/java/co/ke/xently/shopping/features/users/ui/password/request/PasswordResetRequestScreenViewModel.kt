package co.ke.xently.shopping.features.users.ui.password.request

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.ke.xently.shopping.features.users.repositories.IUserRepository
import co.ke.xently.shopping.features.utils.State
import co.ke.xently.shopping.libraries.data.source.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class PasswordResetRequestScreenViewModel @Inject constructor(
    private val repository: IUserRepository,
) : ViewModel() {
    private val emailAddress = MutableSharedFlow<String>()
    private val _passwordResetRequestState = MutableSharedFlow<State<User>>()
    val passwordResetRequestState = _passwordResetRequestState.asSharedFlow()

    init {
        viewModelScope.launch {
            emailAddress.collectLatest { email ->
                repository.requestTemporaryPassword(email).transformLatest { result ->
                    result.onSuccess {
                        emit(State.Success(it))
                    }.onFailure {
                        emit(State.Error(it))
                    }
                }.onStart {
                    emit(State.Loading)
                }.catch {
                    emit(State.Error(it))
                }.collectLatest(_passwordResetRequestState::emit)
            }
        }
    }

    operator fun invoke(email: String) {
        viewModelScope.launch {
            this@PasswordResetRequestScreenViewModel.emailAddress.emit(email)
        }
    }
}