package com.prince.contactsapp.models

import android.app.Application
import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideProfileRepository(appDatabase: AppDatabase): ProfileRepository {
        return ProfileRepository(appDatabase.ProfileDao())
    }

    @Provides
    @Singleton
    fun provideContactRepository(appDatabase: AppDatabase): ContactRepository {
        return ContactRepository(appDatabase.ContactDao())
    }

    @Provides
    @Singleton
    fun provideApplicationContext(application: Application): Context {
        return application.applicationContext
    }
}
