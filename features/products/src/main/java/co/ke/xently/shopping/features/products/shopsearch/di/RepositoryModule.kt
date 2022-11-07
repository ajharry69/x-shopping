package co.ke.xently.shopping.features.products.shopsearch.di

import co.ke.xently.shopping.features.products.shopsearch.ShopSearchRepository
import co.ke.xently.shopping.features.search.repositories.ISearchRepository
import co.ke.xently.shopping.libraries.data.source.Shop
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class RepositoryModule {
    @Binds
    abstract fun bindRepository(repository: ShopSearchRepository): ISearchRepository<Shop>
}