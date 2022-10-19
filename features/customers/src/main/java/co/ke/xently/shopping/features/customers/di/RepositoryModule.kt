package co.ke.xently.shopping.features.customers.di

import co.ke.xently.shopping.features.customers.repositories.CustomerRepository
import co.ke.xently.shopping.features.customers.repositories.CustomerSearchRepository
import co.ke.xently.shopping.features.customers.repositories.ICustomerRepository
import co.ke.xently.shopping.features.search.repositories.ISearchRepository
import co.ke.xently.shopping.libraries.data.source.Customer
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class RepositoryModule {
    @Binds
    abstract fun bindRepository(repository: CustomerRepository): ICustomerRepository

    @Binds
    abstract fun bindSearchRepository(repository: CustomerSearchRepository): ISearchRepository<Customer>
}