package com.mes.core.network

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideCatalogApi(client: NetworkClient): CatalogApi = client.catalogApi

    @Provides
    @Singleton
    fun provideCartApi(client: NetworkClient): CartApi = client.cartApi

    @Provides
    @Singleton
    fun provideAuthApi(client: NetworkClient): AuthApi = client.authApi

    @Provides
    @Singleton
    fun provideOrdersApi(client: NetworkClient): OrdersApi = client.ordersApi

    @Provides
    @Singleton
    fun provideAddressApi(client: NetworkClient): AddressApi = client.addressApi

    @Provides
    @Singleton
    fun provideNotificationApi(client: NetworkClient): NotificationApi = client.notificationApi
}
