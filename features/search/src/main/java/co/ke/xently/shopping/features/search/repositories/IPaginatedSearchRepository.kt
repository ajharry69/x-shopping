package co.ke.xently.shopping.features.search.repositories

import androidx.paging.PagingConfig
import androidx.paging.PagingData
import co.ke.xently.shopping.features.utils.Query
import kotlinx.coroutines.flow.Flow

interface IPaginatedSearchRepository<T : Any> {
    fun get(config: PagingConfig, query: Query): Flow<PagingData<T>>
}