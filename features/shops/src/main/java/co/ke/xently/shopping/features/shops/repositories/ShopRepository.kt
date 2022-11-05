package co.ke.xently.shopping.features.shops.repositories

import co.ke.xently.shopping.features.Dependencies
import co.ke.xently.shopping.features.shops.repositories.exceptions.ShopHttpException
import co.ke.xently.shopping.libraries.data.source.Shop
import co.ke.xently.shopping.libraries.data.source.remote.ExceptionUtils.retryCatch
import co.ke.xently.shopping.libraries.data.source.remote.Http.sendRequest
import co.ke.xently.shopping.libraries.data.source.utils.Retry
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class ShopRepository @Inject constructor(
    private val dependencies: Dependencies,
) : IShopRepository {
    override fun save(shop: Shop) = Retry().run {
        flow {
            emit(sendRequest(ShopHttpException::class) {
                dependencies.service.shop.run {
                    if (shop.id == Shop.DEFAULT_INSTANCE.id) {
                        add(shop)
                    } else {
                        update(shop.id, shop)
                    }
                }
            })
        }.retryCatch(this).onEach { result ->
            result.getOrNull()?.also {
                dependencies.database.shopDao.save()
            }
        }.flowOn(dependencies.dispatcher.io)
    }

    override fun get(id: Long) = Retry().run {
        dependencies.database.shopDao.get(id).mapLatest { item ->
            if (item == null) {
                // Item was not already fetched and cached locally, fetch it from the remote
                // datasource instead.
                sendRequest {
                    dependencies.service.shop.get(id)
                }.also { result ->
                    result.getOrNull()?.also {
                        dependencies.database.shopDao.save(it.asEntity)
                    }
                }
            } else {
                Result.success(item.asUIInstance)
            }
        }.retryCatch(this).flowOn(dependencies.dispatcher.io)
    }
}