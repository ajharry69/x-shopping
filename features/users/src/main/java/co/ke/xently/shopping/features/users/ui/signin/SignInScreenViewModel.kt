package co.ke.xently.shopping.features.users.ui.signin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.ke.xently.shopping.features.users.BasicAuth
import co.ke.xently.shopping.features.users.repositories.IUserRepository
import co.ke.xently.shopping.features.utils.State
import co.ke.xently.shopping.libraries.data.source.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class SignInScreenViewModel @Inject constructor(
    private val repository: IUserRepository,
) : ViewModel() {
    private val basicAuth = MutableSharedFlow<BasicAuth>()
    private val _signInState = MutableSharedFlow<State<User>>()
    val signInState = _signInState.asSharedFlow()

    init {
        viewModelScope.launch {
            basicAuth.collectLatest { auth ->
                repository.signIn(auth).transformLatest { result ->
                    result.onSuccess {
                        emit(State.Success(it))
                    }.onFailure {
                        emit(State.Error(it))
                    }
                }.onStart {
                    emit(State.Loading)
                }.catch {
                    emit(State.Error(it))
                }.collectLatest(_signInState::emit)
            }
        }
    }

    operator fun invoke(basicAuth: BasicAuth) {
        viewModelScope.launch {
            this@SignInScreenViewModel.basicAuth.emit(basicAuth)
        }
    }
}