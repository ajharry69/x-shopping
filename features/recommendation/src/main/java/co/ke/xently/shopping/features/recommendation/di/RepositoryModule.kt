package co.ke.xently.shopping.features.recommendation.di

import co.ke.xently.shopping.features.recommendation.repositories.IRecommendationRepository
import co.ke.xently.shopping.features.recommendation.repositories.RecommendationRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class RepositoryModule {
    @Binds
    abstract fun bindRepository(repository: RecommendationRepository): IRecommendationRepository
}