package co.ke.xently.shopping.features.customers.repositories

import co.ke.xently.shopping.features.Dependencies
import co.ke.xently.shopping.libraries.data.source.Customer
import co.ke.xently.shopping.libraries.data.source.asUIInstance
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class CustomerRepository @Inject constructor(
    private val dependencies: Dependencies,
) : ICustomerRepository {
    override fun get(): Flow<Result<List<Customer>>> {
        return dependencies.database.customerDao.get()
            .flowOn(dependencies.dispatcher.io)
            .map { taxEntities ->
                val taxes = taxEntities.map {
                    it.asUIInstance
                }
                Result.success(taxes)
            }.flowOn(dependencies.dispatcher.computation)
    }

    override fun get(query: String): Flow<Result<List<Customer>>> {
        return dependencies.database.customerDao.get("%$query%")
            .flowOn(dependencies.dispatcher.io)
            .map { taxEntities ->
                val taxes = taxEntities.map {
                    it.asUIInstance
                }
                Result.success(taxes)
            }.flowOn(dependencies.dispatcher.computation)
    }

    override fun get(id: Int): Flow<Result<Customer?>> {
        return dependencies.database.customerDao.get(id)
            .flowOn(dependencies.dispatcher.io)
            .map {
                Result.success(it?.asUIInstance)
            }.flowOn(dependencies.dispatcher.computation)
    }

    override fun delete(id: Int): Flow<Result<Unit>> {
        return flow {
            emit(dependencies.database.customerDao.remove(id))
        }.flowOn(dependencies.dispatcher.io)
            .map { Result.success(Unit) }
    }

    override fun save(customer: Customer): Flow<Result<Unit>> {
        return flow {
            emit(
                dependencies.database.customerDao.run {
                    if (customer.id == Customer.DEFAULT_INSTANCE.id) {
                        add(customer.asEntity)
                    } else {
                        update(customer.asEntity)
                    }
                }
            )
        }.flowOn(dependencies.dispatcher.io)
            .map { Result.success(Unit) }
    }
}