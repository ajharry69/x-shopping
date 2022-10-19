package co.ke.xently.shopping.features.search.repositories

import co.ke.xently.shopping.features.utils.Query
import kotlinx.coroutines.flow.Flow

interface ISearchRepository<T> {
    fun get(query: Query): Flow<Result<List<T>>>
}