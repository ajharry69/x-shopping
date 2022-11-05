package co.ke.xently.shopping.features.shops.repositories.exceptions

import co.ke.xently.shopping.libraries.data.source.remote.HttpException

internal class ShopHttpException(
    val name: List<String> = emptyList(),
    val taxPin: List<String> = emptyList(),
    val coordinates: List<String> = emptyList(),
) : HttpException() {
    override fun hasFieldErrors(): Boolean {
        return arrayOf(
            name,
            taxPin,
            coordinates,
        ).any { it.isNotEmpty() }
    }
}