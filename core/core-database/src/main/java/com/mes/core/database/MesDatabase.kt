package com.mes.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mes.core.database.dao.AddressDao
import com.mes.core.database.dao.CartDao
import com.mes.core.database.dao.ProductCacheDao
import com.mes.core.database.entity.AddressEntity
import com.mes.core.database.entity.CartLineEntity
import com.mes.core.database.entity.ProductCacheEntity

@Database(
    entities = [
        CartLineEntity::class,
        AddressEntity::class,
        ProductCacheEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class MesDatabase : RoomDatabase() {
    abstract fun cartDao(): CartDao
    abstract fun addressDao(): AddressDao
    abstract fun productCacheDao(): ProductCacheDao
}
