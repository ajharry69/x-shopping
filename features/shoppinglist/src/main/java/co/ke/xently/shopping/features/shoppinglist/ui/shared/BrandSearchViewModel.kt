package co.ke.xently.shopping.features.shoppinglist.ui.shared

import co.ke.xently.shopping.features.search.SearchViewModel
import co.ke.xently.shopping.features.search.repositories.ISearchRepository
import co.ke.xently.shopping.libraries.data.source.AbstractBrand
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class BrandSearchViewModel @Inject constructor(
    repository: ISearchRepository<AbstractBrand>,
) : SearchViewModel<AbstractBrand>(repository)