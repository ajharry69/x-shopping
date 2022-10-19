package co.ke.xently.shopping.features.customers.ui.search

import co.ke.xently.shopping.features.search.SearchViewModel
import co.ke.xently.shopping.features.search.repositories.ISearchRepository
import co.ke.xently.shopping.libraries.data.source.Customer
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CustomerSearchViewModel @Inject constructor(
    repository: ISearchRepository<Customer>,
) : SearchViewModel<Customer>(repository)