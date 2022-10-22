package co.ke.xently.shopping.features.shoppinglist.ui.search

import co.ke.xently.shopping.features.search.SearchViewModel
import co.ke.xently.shopping.features.search.repositories.ISearchRepository
import co.ke.xently.shopping.libraries.data.source.ShoppingListItem
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ShoppingListItemSearchViewModel @Inject constructor(
    repository: ISearchRepository<ShoppingListItem>,
) : SearchViewModel<ShoppingListItem>(repository)