package co.ke.xently.shopping.features.users.repositories.exceptions

import co.ke.xently.shopping.libraries.data.source.remote.HttpException

internal class SignUpHttpException(
    val name: List<String> = emptyList(),
    val email: List<String> = emptyList(),
    val password: List<String> = emptyList(),
) : HttpException() {
    override fun hasFieldErrors(): Boolean {
        return arrayOf(name, email, password).any { it.isNotEmpty() }
    }
}