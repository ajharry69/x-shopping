package co.ke.xently.shopping.features.shops.search.di

import co.ke.xently.shopping.features.search.repositories.IPaginatedSearchRepository
import co.ke.xently.shopping.features.shops.search.repositories.ShopPaginatedSearchRepository
import co.ke.xently.shopping.libraries.data.source.Shop
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class RepositoryModule {
    @Binds
    abstract fun bindRepository(repository: ShopPaginatedSearchRepository): IPaginatedSearchRepository<Shop>
}