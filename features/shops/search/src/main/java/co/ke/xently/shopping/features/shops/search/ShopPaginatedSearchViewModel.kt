package co.ke.xently.shopping.features.shops.search

import co.ke.xently.shopping.features.search.PaginatedSearchViewModel
import co.ke.xently.shopping.features.search.repositories.IPaginatedSearchRepository
import co.ke.xently.shopping.libraries.data.source.Shop
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ShopPaginatedSearchViewModel @Inject constructor(
    repository: IPaginatedSearchRepository<Shop>,
) : PaginatedSearchViewModel<Shop>(repository)