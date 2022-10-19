package co.ke.xently.shopping.features.users.ui.password.reset

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
internal class PasswordResetScreenViewModel @Inject constructor(
    private val repository: IUserRepository,
) : ViewModel() {
    private val resetPassword = MutableSharedFlow<User.ResetPassword>()
    private val _passwordResetState = MutableSharedFlow<State<User>>()
    val passwordResetState = _passwordResetState.asSharedFlow()

    init {
        viewModelScope.launch {
            resetPassword.collectLatest { resetPassword ->
                repository.resetPassword(resetPassword).transformLatest { result ->
                    result.onSuccess {
                        emit(State.Success(it))
                    }.onFailure {
                        emit(State.Error(it))
                    }
                }.onStart {
                    emit(State.Loading)
                }.catch {
                    emit(State.Error(it))
                }.collectLatest(_passwordResetState::emit)
            }
        }
    }

    operator fun invoke(resetPassword: User.ResetPassword) {
        viewModelScope.launch {
            this@PasswordResetScreenViewModel.resetPassword.emit(resetPassword)
        }
    }
}