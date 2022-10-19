package co.ke.xently.shopping.features.users.repositories

import androidx.core.content.edit
import co.ke.xently.shopping.features.Dependencies
import co.ke.xently.shopping.features.users.BasicAuth
import co.ke.xently.shopping.features.users.repositories.exceptions.*
import co.ke.xently.shopping.libraries.data.source.User
import co.ke.xently.shopping.libraries.data.source.asUIInstance
import co.ke.xently.shopping.libraries.data.source.remote.ExceptionUtils.retryCatch
import co.ke.xently.shopping.libraries.data.source.remote.Http.sendRequest
import co.ke.xently.shopping.libraries.data.source.utils.Retry
import co.ke.xently.shopping.libraries.data.source.utils.SharedPreferenceKey
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import okhttp3.Credentials
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class UserRepository @Inject constructor(
    private val dependencies: Dependencies,
) : IUserRepository {
    private fun Flow<Result<User>>.saveLocallyWithRetryIfApplicable(
        retry: Retry,
        isPasswordResetRequested: Boolean = false,
    ) = onEach { result ->
        result.getOrNull()?.run {
            asEntity.also {
                dependencies.database.userDao.save(it.copy(
                    isPasswordResetRequested = isPasswordResetRequested))
            }
            if (token != null) {
                dependencies.preference.encrypted.edit(commit = true) {
                    putString(SharedPreferenceKey.TOKEN_VALUE, token)
                }
            }
        }
    }.retryCatch(retry).flowOn(dependencies.dispatcher.io)

    override fun getAuthenticated() = dependencies.database.userDao.getAuthenticated()
        .flowOn(dependencies.dispatcher.io)
        .map {
            Result.success(it?.asUIInstance)
        }.flowOn(dependencies.dispatcher.computation)

    override fun signIn(basicAuth: BasicAuth) = Retry().run {
        flow {
            emit(
                sendRequest(SignInHttpException::class.java) {
                    val basicAuthData = Credentials.basic(basicAuth.username, basicAuth.password)
                    dependencies.service.account.signIn(basicAuthData)
                },
            )
        }.saveLocallyWithRetryIfApplicable(this)
    }

    override fun signOut() = Retry().run {
        flow {
            emit(dependencies.database.userDao.getCurrentlyActiveUserID())
        }.map {
//            dependencies.database.clearAllTables()
            dependencies.database.userDao.clear()
            @Suppress("BlockingMethodInNonBlockingContext")
            dependencies.cache.evictAll()
            dependencies.preference.encrypted.run {
                getString(SharedPreferenceKey.TOKEN_VALUE, null)?.apply {
                    edit {
                        remove(SharedPreferenceKey.TOKEN_VALUE)
                    }
                }
            }?.also { token ->
                coroutineScope {
                    launch(dependencies.dispatcher.io) {
                        sendRequest {
                            dependencies.service.account.signout(it, "Bearer $token")
                        }
                    }
                }
            }
            Result.success(it)
        }.flowOn(dependencies.dispatcher.io)
    }

    override fun signUp(user: User) = Retry().run {
        flow {
            emit(
                sendRequest(SignUpHttpException::class.java) {
                    dependencies.service.account.signUp(user)
                },
            )
        }.saveLocallyWithRetryIfApplicable(this)
    }

    override fun resetPassword(resetPassword: User.ResetPassword) = Retry().run {
        flow {
            emit(
                sendRequest(PasswordResetHttpException::class.java) {
                    dependencies.service.account.resetPassword(
                        dependencies.database.userDao.getCurrentlyActiveUserID(),
                        resetPassword,
                    )
                },
            )
        }.saveLocallyWithRetryIfApplicable(this)
    }

    override fun requestTemporaryPassword(email: String) = Retry().run {
        flow {
            emit(
                sendRequest(PasswordResetRequestHttpException::class.java) {
                    dependencies.service.account.requestTemporaryPassword(mapOf("email" to email))
                },
            )
        }.saveLocallyWithRetryIfApplicable(this, true)
    }

    override fun requestVerificationCode() = Retry().run {
        flow {
            emit(
                sendRequest {
                    dependencies.service.account.requestVerificationCode(
                        dependencies.database.userDao.getCurrentlyActiveUserID())
                },
            )
        }.saveLocallyWithRetryIfApplicable(this)
    }

    override fun verifyAccount(code: String) = Retry().run {
        flow {
            emit(
                sendRequest(VerificationHttpException::class.java) {
                    dependencies.service.account.verify(
                        dependencies.database.userDao.getCurrentlyActiveUserID(),
                        mapOf("code" to code),
                    )
                },
            )
        }.saveLocallyWithRetryIfApplicable(this)
    }
}