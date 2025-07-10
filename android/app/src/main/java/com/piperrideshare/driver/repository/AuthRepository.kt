package com.piperrideshare.driver.repository

import com.piperrideshare.driver.api.ApiService
import com.piperrideshare.driver.api.AuthService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthRepository {

    @Provides
    @Singleton
    fun provideAuthService(apiService: ApiService): AuthService {
        AuthService.init(apiService)
        return AuthService
    }
}
