package co.ke.xently.shopping.features.products.di

import co.ke.xently.shopping.features.products.repositories.IProductRepository
import co.ke.xently.shopping.features.products.repositories.ProductRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class RepositoryModule {
    @Binds
    abstract fun bindRepository(repository: ProductRepository): IProductRepository
}