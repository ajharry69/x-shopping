package co.ke.xently.shopping.features.products.repositories

import co.ke.xently.shopping.features.Dependencies
import co.ke.xently.shopping.features.products.repositories.exceptions.ProductHttpException
import co.ke.xently.shopping.libraries.data.source.Product
import co.ke.xently.shopping.libraries.data.source.remote.ExceptionUtils.retryCatch
import co.ke.xently.shopping.libraries.data.source.remote.Http.sendRequest
import co.ke.xently.shopping.libraries.data.source.utils.Retry
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class ProductRepository @Inject constructor(
    private val dependencies: Dependencies,
) : IProductRepository {
    override fun save(product: Product) = Retry().run {
        flow {
            emit(sendRequest(ProductHttpException::class) {
                dependencies.service.product.run {
                    if (product.id == Product.DEFAULT_INSTANCE.id) {
                        add(product.asResource)
                    } else {
                        update(product.id, product.asResource)
                    }
                }
            })
        }.retryCatch(this).map { result ->
            result.getOrNull()?.let {
                it.saveLocally(dependencies)
                Result.success(dependencies.database.productDao.get(it.id).single()!!.asUIInstance)
            } ?: Result.failure(result.exceptionOrNull()!!)
        }.flowOn(dependencies.dispatcher.io)
    }

    override fun get(id: Long) = Retry().run {
        dependencies.database.productDao.get(id).map { entity ->
            if (entity == null) {
                // Item was not already fetched and cached locally, fetch it from the remote
                // datasource instead.
                sendRequest {
                    dependencies.service.product.get(id)
                }.let { result ->
                    result.getOrNull()?.asEntity?.let {
                        dependencies.database.productDao.save(it)
                        Result.success(dependencies.database.productDao.get(it.id)
                            .single()!!.asUIInstance)
                    } ?: Result.failure(result.exceptionOrNull()!!)
                }
            } else {
                Result.success(entity.asUIInstance)
            }
        }.retryCatch(this).flowOn(dependencies.dispatcher.io)
    }
}