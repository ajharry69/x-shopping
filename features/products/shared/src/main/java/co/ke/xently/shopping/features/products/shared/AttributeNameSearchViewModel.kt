package co.ke.xently.shopping.features.products.shared

import co.ke.xently.shopping.features.search.SearchViewModel
import co.ke.xently.shopping.features.search.repositories.ISearchRepository
import co.ke.xently.shopping.features.products.shared.di.qualifiers.SearchAttributeName
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AttributeNameSearchViewModel @Inject constructor(
    @SearchAttributeName
    repository: ISearchRepository<String>,
) : SearchViewModel<String>(repository)