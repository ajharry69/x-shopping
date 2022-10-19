package co.ke.xently.shopping.features.users.repositories

import co.ke.xently.shopping.features.users.BasicAuth
import co.ke.xently.shopping.libraries.data.source.User
import kotlinx.coroutines.flow.Flow

interface IUserRepository {
    fun getAuthenticated(): Flow<Result<User?>>
    fun signUp(user: User): Flow<Result<User>>
    fun signIn(basicAuth: BasicAuth): Flow<Result<User?>>
    fun signOut(): Flow<Result<Long>>
    fun resetPassword(resetPassword: User.ResetPassword): Flow<Result<User>>
    fun requestTemporaryPassword(email: String): Flow<Result<User>>
    fun requestVerificationCode(): Flow<Result<User>>
    fun verifyAccount(code: String): Flow<Result<User>>
}