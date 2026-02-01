package com.piperrideshare.driver.di

import android.app.NotificationManager
import android.content.Context
import com.piperrideshare.driver.data.UserPreferences
import com.piperrideshare.driver.services.session.ISessionManager
import com.piperrideshare.driver.services.session.SessionManager
import com.piperrideshare.driver.services.MapboxSearchService

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

import com.piperrideshare.driver.services.DriverStateManager
import com.piperrideshare.driver.services.IDriverStateManager

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideNotificationManager(
        @ApplicationContext context: Context,
    ): NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    @Provides
    @Singleton
    fun provideSessionManager(
        @ApplicationContext context: Context,
    ): ISessionManager = SessionManager(context)

    @Module
    @InstallIn(SingletonComponent::class)
    object PreferencesModule {
        @Provides
        @Singleton
        fun provideUserPreferences(
            @ApplicationContext context: Context,
        ): UserPreferences = UserPreferences(context)
    }

    // styamamo - edit
    @Provides
    @Singleton
    fun provideMapboxSearchService(
        @ApplicationContext context: Context
    ): MapboxSearchService = MapboxSearchService(context)
}
