package co.ke.xently.shopping.features.users.repositories.exceptions

import co.ke.xently.shopping.libraries.data.source.remote.HttpException

internal class SignInHttpException(
    val email: List<String> = emptyList(),
    val password: List<String> = emptyList(),
) : HttpException() {
    override fun hasFieldErrors(): Boolean {
        return arrayOf(email, password).any { it.isNotEmpty() }
    }
}