package com.example.solanamobiledappscaffold.data.di

import com.example.solanamobiledappscaffold.data.repository.WalletRepositoryImpl
import com.example.solanamobiledappscaffold.domain.repository.WalletRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideWalletRepository(): WalletRepository = WalletRepositoryImpl()
}
