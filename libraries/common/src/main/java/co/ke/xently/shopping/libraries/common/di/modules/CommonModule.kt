package co.ke.xently.shopping.libraries.common.di.modules

import co.ke.xently.shopping.libraries.common.di.qualifiers.coroutines.ComputationDispatcher
import co.ke.xently.shopping.libraries.common.di.qualifiers.coroutines.IODispatcher
import co.ke.xently.shopping.libraries.common.di.qualifiers.coroutines.UIDispatcher
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

@Module
@InstallIn(SingletonComponent::class)
object CommonModule {
    @Provides
//    @Singleton
    @IODispatcher
    fun provideIODispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
//    @Singleton
    @ComputationDispatcher
    fun provideComputationDispatcher(): CoroutineDispatcher = Dispatchers.Default

    @Provides
//    @Singleton
    @UIDispatcher
    fun provideUIDispatcher(): CoroutineDispatcher = Dispatchers.Main
}