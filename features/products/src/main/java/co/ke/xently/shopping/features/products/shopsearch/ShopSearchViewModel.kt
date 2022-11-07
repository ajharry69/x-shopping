package co.ke.xently.shopping.features.products.shopsearch

import co.ke.xently.shopping.features.search.SearchViewModel
import co.ke.xently.shopping.features.search.repositories.ISearchRepository
import co.ke.xently.shopping.libraries.data.source.Shop
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ShopSearchViewModel @Inject constructor(repository: ISearchRepository<Shop>) :
    SearchViewModel<Shop>(repository)