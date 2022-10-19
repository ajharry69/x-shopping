package co.ke.xently.shopping.features.customers.repositories

import co.ke.xently.shopping.libraries.data.source.Customer
import kotlinx.coroutines.flow.Flow

interface ICustomerRepository {
    fun get(): Flow<Result<List<Customer>>>
    fun get(query: String): Flow<Result<List<Customer>>>
    fun get(id: Int): Flow<Result<Customer?>>
    fun delete(id: Int): Flow<Result<Unit>>
    fun save(customer: Customer): Flow<Result<Unit>>
}