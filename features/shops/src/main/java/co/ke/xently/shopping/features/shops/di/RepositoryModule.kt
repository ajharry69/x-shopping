package co.ke.xently.shopping.features.shops.di

import co.ke.xently.shopping.features.shops.repositories.IShopRepository
import co.ke.xently.shopping.features.shops.repositories.ShopRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class RepositoryModule {
    @Binds
    abstract fun bindRepository(repository: ShopRepository): IShopRepository
}