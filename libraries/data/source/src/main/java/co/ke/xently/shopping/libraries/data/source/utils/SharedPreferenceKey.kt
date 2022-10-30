package co.ke.xently.shopping.libraries.data.source.utils

object SharedPreferenceKey {
    private const val PREFIX =
        "co.ke.xently.shopping.libraries.data.source.utils.SharedPreferenceKey"
    const val ENCRYPTED = "$PREFIX.ENCRYPTED"
    const val UNENCRYPTED = "$PREFIX.UNENCRYPTED"
    const val TOKEN_VALUE = "$PREFIX.TOKEN_VALUE"
}