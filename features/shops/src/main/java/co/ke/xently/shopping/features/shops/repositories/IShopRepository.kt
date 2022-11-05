package co.ke.xently.shopping.features.shops.repositories

import co.ke.xently.shopping.libraries.data.source.Shop
import kotlinx.coroutines.flow.Flow

interface IShopRepository {
    fun save(shop: Shop): Flow<Result<Shop>>
    fun get(id: Long): Flow<Result<Shop?>>
}