package com.piperrideshare.driver.di

import com.piperrideshare.driver.services.DriverStateManager
import com.piperrideshare.driver.services.IDriverStateManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class StateModule {
    @Binds
    @Singleton
    abstract fun bindDriverStateManager(impl: DriverStateManager): IDriverStateManager
}