package co.ke.xently.shopping.features.shoppinglist.di

import co.ke.xently.shopping.features.search.repositories.ISearchRepository
import co.ke.xently.shopping.features.shoppinglist.repositories.IShoppingListRepository
import co.ke.xently.shopping.features.shoppinglist.repositories.ShoppingListItemSearchRepository
import co.ke.xently.shopping.features.shoppinglist.repositories.ShoppingListRepository
import co.ke.xently.shopping.libraries.data.source.ShoppingListItem
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class RepositoryModule {
    @Binds
    abstract fun bindRepository(repository: ShoppingListRepository): IShoppingListRepository

    @Binds
    abstract fun bindSearchRepository(repository: ShoppingListItemSearchRepository): ISearchRepository<ShoppingListItem>
}