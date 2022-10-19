package co.ke.xently.shopping.features.users.ui.signup

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
internal class SignUpScreenViewModel @Inject constructor(
    private val repository: IUserRepository,
) : ViewModel() {
    private val user = MutableSharedFlow<User>()
    private val _signUpState = MutableSharedFlow<State<User>>()
    val signUpState = _signUpState.asSharedFlow()

    init {
        viewModelScope.launch {
            user.collectLatest { user ->
                repository.signUp(user).transformLatest { result ->
                    result.onSuccess {
                        emit(State.Success(user))
                    }.onFailure {
                        emit(State.Error(it))
                    }
                }.onStart {
                    emit(State.Loading)
                }.catch {
                    emit(State.Error(it))
                }.collectLatest(_signUpState::emit)
            }
        }
    }

    fun save(user: User) {
        viewModelScope.launch {
            this@SignUpScreenViewModel.user.emit(user)
        }
    }
}