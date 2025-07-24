package com.piperrideshare.driver.di

import com.piperrideshare.driver.services.session.ISessionManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface AppEntryPoint {
    fun sessionManager(): ISessionManager
}
