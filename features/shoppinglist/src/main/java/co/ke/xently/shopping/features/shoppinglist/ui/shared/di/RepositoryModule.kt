package co.ke.xently.shopping.features.shoppinglist.ui.shared.di

import co.ke.xently.shopping.features.search.repositories.ISearchRepository
import co.ke.xently.shopping.features.shoppinglist.ui.shared.repositories.AttributeNameSearchRepository
import co.ke.xently.shopping.features.shoppinglist.ui.shared.repositories.AttributeSearchRepository
import co.ke.xently.shopping.features.shoppinglist.ui.shared.repositories.BrandSearchRepository
import co.ke.xently.shopping.libraries.data.source.AbstractAttribute
import co.ke.xently.shopping.libraries.data.source.AbstractBrand
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class RepositoryModule {
    @Binds
    abstract fun bindBrandSearchRepository(repository: BrandSearchRepository): ISearchRepository<AbstractBrand>

    @Binds
    abstract fun bindAttributeSearchRepository(repository: AttributeSearchRepository): ISearchRepository<AbstractAttribute>

    @Binds
    abstract fun bindAttributeNameSearchRepository(repository: AttributeNameSearchRepository): ISearchRepository<String>
}