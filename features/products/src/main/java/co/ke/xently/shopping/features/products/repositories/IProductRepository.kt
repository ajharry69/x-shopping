package co.ke.xently.shopping.features.products.repositories

import co.ke.xently.shopping.features.products.models.Product
import kotlinx.coroutines.flow.Flow

interface IProductRepository {
    fun save(product: Product): Flow<Result<Product>>
    fun get(id: Long): Flow<Result<Product?>>
}