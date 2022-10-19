package co.ke.xently.shopping.features.users.ui.verification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.ke.xently.shopping.features.users.repositories.IUserRepository
import co.ke.xently.shopping.features.utils.State
import co.ke.xently.shopping.libraries.data.source.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
internal class VerificationScreenViewModel @Inject constructor(
    private val repository: IUserRepository,
) : ViewModel() {
    private val verificationCode = MutableSharedFlow<String>()
    private val _verificationState = MutableSharedFlow<State<User>>()
    val verificationState = _verificationState.asSharedFlow()

    private val resendVerificationCode = MutableSharedFlow<Int>()
    private val _resendVerificationCodeState = MutableSharedFlow<State<User>>()
    val resendVerificationCodeState = _resendVerificationCodeState.asSharedFlow()

    init {
        viewModelScope.launch {
            verificationCode.collectLatest { code ->
                repository.verifyAccount(code).transformLatest { result ->
                    result.onSuccess {
                        emit(State.Success(it))
                    }.onFailure {
                        emit(State.Error(it))
                    }
                }.onStart {
                    emit(State.Loading)
                }.catch {
                    emit(State.Error(it))
                }.collectLatest(_verificationState::emit)
            }
        }
        viewModelScope.launch {
            resendVerificationCode.collectLatest { _ ->
                repository.requestVerificationCode().transformLatest { result ->
                    result.onSuccess {
                        emit(State.Success(it))
                    }.onFailure {
                        emit(State.Error(it))
                    }
                }.onStart {
                    emit(State.Loading)
                }.catch {
                    emit(State.Error(it))
                }.collectLatest(_resendVerificationCodeState::emit)
            }
        }
    }

    operator fun invoke(code: String) {
        viewModelScope.launch {
            this@VerificationScreenViewModel.verificationCode.emit(code)
        }
    }

    fun resendCode() {
        viewModelScope.launch {
            this@VerificationScreenViewModel.resendVerificationCode.emit(Random(1).nextInt())
        }
    }
}