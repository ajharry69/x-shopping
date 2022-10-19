package co.ke.xently.shopping.libraries.common

import co.ke.xently.shopping.libraries.common.di.qualifiers.coroutines.ComputationDispatcher
import co.ke.xently.shopping.libraries.common.di.qualifiers.coroutines.IODispatcher
import co.ke.xently.shopping.libraries.common.di.qualifiers.coroutines.UIDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
data class Dispatcher @Inject constructor(
    @IODispatcher
    val io: CoroutineDispatcher,
    @UIDispatcher
    val main: CoroutineDispatcher,
    @ComputationDispatcher
    val computation: CoroutineDispatcher,
) {
    val default: CoroutineDispatcher = computation
}
