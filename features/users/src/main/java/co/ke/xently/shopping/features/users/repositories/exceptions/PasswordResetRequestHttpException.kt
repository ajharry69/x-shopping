package co.ke.xently.shopping.features.users.repositories.exceptions

import co.ke.xently.shopping.libraries.data.source.remote.HttpException

internal class PasswordResetRequestHttpException(
    val email: List<String> = emptyList(),
) : HttpException() {
    override fun hasFieldErrors(): Boolean {
        return email.isNotEmpty()
    }
}