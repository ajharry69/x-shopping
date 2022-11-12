package co.ke.xently.shopping.features.shops.search.repositories

import androidx.paging.Pager
import androidx.paging.PagingConfig
import co.ke.xently.shopping.features.Dependencies
import co.ke.xently.shopping.features.search.repositories.IPaginatedSearchRepository
import co.ke.xently.shopping.features.utils.Query
import co.ke.xently.shopping.libraries.data.source.Shop
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShopPaginatedSearchRepository @Inject constructor(private val dependencies: Dependencies) :
    IPaginatedSearchRepository<Shop> {
    override fun get(config: PagingConfig, query: Query) = Pager(
        config = config,
        pagingSourceFactory = {
            ShopNetworkPagingSource(query.value, dependencies)
        },
    ).flow
}