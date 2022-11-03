package co.ke.xently.shopping.features.shoppinglist.ui.shared.di

import co.ke.xently.shopping.features.search.repositories.ISearchRepository
import co.ke.xently.shopping.features.shoppinglist.ui.shared.repositories.BrandSearchRepository
import co.ke.xently.shopping.libraries.data.source.AbstractBrand
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class RepositoryModule {
    @Binds
    abstract fun bindSearchRepository(repository: BrandSearchRepository): ISearchRepository<AbstractBrand>
}