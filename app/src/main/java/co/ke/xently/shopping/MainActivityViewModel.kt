package co.ke.xently.shopping

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.ke.xently.shopping.features.users.repositories.IUserRepository
import co.ke.xently.shopping.features.utils.State
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class MainActivityViewModel @Inject constructor(
    private val userRepository: IUserRepository,
) : ViewModel() {
    val userState = userRepository.getAuthenticated().transformLatest { result ->
        result.onSuccess {
            it?.also { user ->
                Firebase.crashlytics.setUserId("#${user.id}=> ${user.email}")
            }
            emit(State.Success(it))
        }.onFailure {
            emit(State.Error(it))
        }
    }.onStart {
        emit(State.Loading)
    }.catch {
        emit(State.Error(it))
    }.shareIn(viewModelScope, SharingStarted.WhileSubscribed(replayExpirationMillis = 5_000))

    private val signOut = MutableSharedFlow<Boolean>()
    private val _signOutState = MutableSharedFlow<State<Long>>()
    val signOutState = _signOutState.asSharedFlow()

    init {
        viewModelScope.launch {
            signOut.collectLatest {
                userRepository.signOut().transformLatest { result ->
                    result.onSuccess {
                        emit(State.Success(it))
                    }.onFailure {
                        emit(State.Error(it))
                    }
                }.onStart {
                    emit(State.Loading)
                }.catch {
                    emit(State.Error(it))
                }.collectLatest(_signOutState::emit)
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            signOut.emit(true)
        }
    }
}