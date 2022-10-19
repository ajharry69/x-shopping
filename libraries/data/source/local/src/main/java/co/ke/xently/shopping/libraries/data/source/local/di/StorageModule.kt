package co.ke.xently.shopping.libraries.data.source.local.di

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import co.ke.xently.shopping.libraries.data.source.di.qualifiers.EncryptedSharedPreference
import co.ke.xently.shopping.libraries.data.source.di.qualifiers.UnencryptedSharedPreference
import co.ke.xently.shopping.libraries.data.source.local.BuildConfig
import co.ke.xently.shopping.libraries.data.source.local.Database
import co.ke.xently.shopping.libraries.data.source.utils.SharedPreferenceKey.ENCRYPTED
import co.ke.xently.shopping.libraries.data.source.utils.SharedPreferenceKey.UNENCRYPTED
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import timber.log.Timber
import java.util.concurrent.Executors
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object StorageModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): Database {
        return Room.databaseBuilder(context, Database::class.java, "${context.packageName}.pos.db")
            .fallbackToDestructiveMigration()
            .apply {
                if (BuildConfig.DEBUG) {
                    setQueryCallback(
                        { query, args ->
                            Timber.d("Query <${query}>. Args: <${args.joinToString()}>")
                        },
                        Executors.newSingleThreadExecutor(),
                    )
                }
            }.build()
    }

    @Provides
    @EncryptedSharedPreference
    @Singleton
    fun provideEncryptedSharedPreference(
        @ApplicationContext context: Context,
    ): SharedPreferences = EncryptedSharedPreferences.create(
        context,
        ENCRYPTED,
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    @Provides
    @UnencryptedSharedPreference
    @Singleton
    fun provideUnencryptedSharedPreference(
        @ApplicationContext context: Context,
    ): SharedPreferences =
        context.getSharedPreferences(UNENCRYPTED, Context.MODE_PRIVATE)
}