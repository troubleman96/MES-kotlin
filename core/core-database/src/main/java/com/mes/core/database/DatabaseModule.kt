package com.mes.core.database

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MesDatabase {
        return Room.databaseBuilder(
            context,
            MesDatabase::class.java,
            "mes_database"
        ).fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideCartDao(db: MesDatabase) = db.cartDao()

    @Provides
    fun provideAddressDao(db: MesDatabase) = db.addressDao()

    @Provides
    fun provideProductCacheDao(db: MesDatabase) = db.productCacheDao()
}
