package com.piperrideshare.driver.di

import com.piperrideshare.driver.api.WebSocketHandler
import com.piperrideshare.driver.domain.repository.AuthRepository
import com.piperrideshare.driver.domain.repository.WebSocketRepository
import com.piperrideshare.driver.repository.AuthRepositoryImpl
import com.piperrideshare.driver.services.IWebSocketRepository
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
    @Provides
    @Singleton
    fun provideWebSocketHandler(): WebSocketHandler = WebSocketHandler()
}
