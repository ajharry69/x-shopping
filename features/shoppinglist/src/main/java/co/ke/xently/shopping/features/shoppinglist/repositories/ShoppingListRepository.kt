package co.ke.xently.shopping.features.shoppinglist.repositories

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.map
import co.ke.xently.shopping.features.Dependencies
import co.ke.xently.shopping.features.shoppinglist.GroupBy
import co.ke.xently.shopping.libraries.data.source.ShoppingListItem
import co.ke.xently.shopping.libraries.data.source.remote.ExceptionUtils.retryCatch
import co.ke.xently.shopping.libraries.data.source.remote.Http.sendRequest
import co.ke.xently.shopping.libraries.data.source.utils.DateFormat
import co.ke.xently.shopping.libraries.data.source.utils.Retry
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class ShoppingListRepository @Inject constructor(
    private val dependencies: Dependencies,
) : IShoppingListRepository {
    override fun save(shoppingListItem: ShoppingListItem) = Retry().run {
        flow {
            emit(sendRequest {
                dependencies.service.shoppingList.run {
                    if (shoppingListItem.id == ShoppingListItem.DEFAULT_INSTANCE.id) {
                        add(shoppingListItem)
                    } else {
                        update(shoppingListItem.id, shoppingListItem)
                    }
                }
            })
        }.retryCatch(this).onEach { result ->
            result.getOrNull()?.also {
                it.saveLocally(dependencies)
            }
        }.flowOn(dependencies.dispatcher.io)
    }

    override fun get(config: PagingConfig, groupBy: GroupBy) = Pager(
        config = config,
        pagingSourceFactory = {
            GroupedShoppingListPagingSource(groupBy, dependencies)
        },
    ).flow

    override fun getCount(groupBy: GroupBy) = when (groupBy) {
        GroupBy.DateAdded -> dependencies.database.shoppingListDao.getCountGroupedByDateAdded()
    }.mapLatest {
        buildMap<Any, Int> {
            for (item in it) {
                put(item.group, item.numberOfItems)
            }
        }
    }

    override fun get(id: Long) = Retry().run {
        dependencies.database.shoppingListDao.get(id).mapLatest { item ->
            if (item == null) {
                // Item was not already fetched and cached locally, fetch it from the remote
                // datasource instead.
                sendRequest {
                    dependencies.service.shoppingList.get(id)
                }.also { result ->
                    result.getOrNull()?.also {
                        dependencies.database.shoppingListDao.save(it.asEntity)
                    }
                }
            } else {
                Result.success(item.asUIInstance)
            }
        }.retryCatch(this).flowOn(dependencies.dispatcher.io)
    }

    override fun get(config: PagingConfig, group: ShoppingListGroup?) = Pager(
        config = config,
        remoteMediator = ShoppingListRemoteMediator(group, dependencies),
        pagingSourceFactory = {
            if (group != null && group.groupBy == GroupBy.DateAdded) {
                dependencies.database.shoppingListDao.get(
                    DateFormat.DEFAULT_SERVER_DATE_FORMAT.parse(group.group.toString())!!,
                )
            } else {
                dependencies.database.shoppingListDao.get()
            }
        },
    ).flow.map { data ->
        data.map { it.asUIInstance }
    }

    override fun remove(id: Long) = Retry().run {
        flow {
            emit(dependencies.database.shoppingListDao.delete(id))
        }.map {
            sendRequest {
                dependencies.service.shoppingList.remove(id)
            }
        }.retryCatch(this).flowOn(dependencies.dispatcher.io)
    }
}