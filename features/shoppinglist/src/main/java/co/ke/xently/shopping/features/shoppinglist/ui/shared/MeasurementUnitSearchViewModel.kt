package co.ke.xently.shopping.features.shoppinglist.ui.shared

import co.ke.xently.shopping.features.search.SearchViewModel
import co.ke.xently.shopping.features.search.repositories.ISearchRepository
import co.ke.xently.shopping.features.shoppinglist.ui.shared.di.qualifiers.SearchMeasurementUnit
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MeasurementUnitSearchViewModel @Inject constructor(
    @SearchMeasurementUnit
    repository: ISearchRepository<String>,
) : SearchViewModel<String>(repository)