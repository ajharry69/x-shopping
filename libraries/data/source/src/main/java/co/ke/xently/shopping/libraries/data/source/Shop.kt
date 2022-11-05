package co.ke.xently.shopping.libraries.data.source

import co.ke.xently.shopping.libraries.data.source.local.ShopEntity

data class Shop(
    val id: Long,
    val name: String,
    val taxPin: String,
    val coordinates: Coordinate,
) {
    val asEntity
        get() = ShopEntity(
            id = id,
            name = name,
            taxPin = taxPin,
            coordinates = coordinates,
        )

    companion object {
        val DEFAULT_INSTANCE = Shop(
            id = -1L,
            name = "Xently Shopping",
            taxPin = "A000111222B",
            coordinates = Coordinate(0.0, 0.0),
        )
    }
}