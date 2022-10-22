package co.ke.xently.shopping.features.shoppinglist.ui.shared

import co.ke.xently.shopping.features.search.SearchViewModel
import co.ke.xently.shopping.features.search.repositories.ISearchRepository
import co.ke.xently.shopping.libraries.data.source.AbstractAttribute
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AttributeSearchViewModel @Inject constructor(
    repository: ISearchRepository<AbstractAttribute>,
) : SearchViewModel<AbstractAttribute>(repository)