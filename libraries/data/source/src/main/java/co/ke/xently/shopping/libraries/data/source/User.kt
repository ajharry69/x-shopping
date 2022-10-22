package co.ke.xently.shopping.libraries.data.source

import co.ke.xently.shopping.libraries.data.source.local.UserEntity

data class User(
    val id: Long,
    val name: String,
    val email: String,
    val isActive: Boolean,
    val isVerified: Boolean,
    val shopsCount: Int,
    val password: String = "",
    val token: String? = null,
    val isPasswordResetRequested: Boolean = false,
) {
    data class ResetPassword(
        val oldPassword: String,
        val newPassword: String,
        val isChange: Boolean = false, // TODO: Rename to `considerAsChange`...
    )

    val isSignedIn: Boolean = isActive && isVerified && !isPasswordResetRequested

    val asEntity
        get() = UserEntity(
            id = id,
            name = name,
            email = email,
            isActive = isActive,
            shopsCount = shopsCount,
            isVerified = isVerified,
            isPasswordResetRequested = isPasswordResetRequested,
        )

    companion object {
        val DEFAULT_INSTANCE = User(
            id = 0,
            name = "John Doe",
            email = "",
            isActive = false,
            shopsCount = 0,
            isVerified = false,
            password = "",
        )
    }
}
