package com.example.solanamobiledappscaffold.data.di

import android.content.Context
import com.example.solanamobiledappscaffold.data.repository.WalletRepositoryImpl
import com.example.solanamobiledappscaffold.domain.repository.WalletRepository
import com.example.solanamobiledappscaffold.domain.use_case.basic_storage.BasicPublicKeyStorageUseCase
import com.example.solanamobiledappscaffold.domain.use_case.transaction_usecase.BalanceUseCase
import com.example.solanamobiledappscaffold.domain.use_case.transaction_usecase.RequestAirdropUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule() {

    @Provides
    @Singleton
    fun provideWalletRepository(): WalletRepository = WalletRepositoryImpl()

    @Provides
    @Singleton
    fun provideBalanceUseCase(): BalanceUseCase = BalanceUseCase()

    @Provides
    @Singleton
    fun provideRequestAirdropUseCase(): RequestAirdropUseCase = RequestAirdropUseCase()


    @Provides
    @Singleton
    fun provideBasicPublicKeyStorageUseCase(@ApplicationContext context: Context): BasicPublicKeyStorageUseCase =
        BasicPublicKeyStorageUseCase(context)
}
