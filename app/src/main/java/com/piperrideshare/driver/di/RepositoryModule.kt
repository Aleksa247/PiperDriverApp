package com.piperrideshare.driver.di

import com.piperrideshare.driver.domain.repository.AuthRepository
import com.piperrideshare.driver.domain.repository.WebSocketRepository
import com.piperrideshare.driver.repository.AuthRepositoryImpl
import com.piperrideshare.driver.services.IWebSocketRepository
import android.content.Context
import com.piperrideshare.driver.services.H3Service
import com.piperrideshare.driver.utils.LocationTracker
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindWebSocketRepository(impl: WebSocketRepository): IWebSocketRepository
}

@Module
@InstallIn(SingletonComponent::class)
object RepositoryProvidesModule {
    // Note: WebSocketHandler is now automatically provided by Hilt via @Inject constructor

    @Provides
    @Singleton
    fun provideH3Service(): H3Service = H3Service()

    @Provides
    @Singleton
    fun provideLocationTracker(
        @ApplicationContext context: Context
    ): LocationTracker = LocationTracker(context)
}

