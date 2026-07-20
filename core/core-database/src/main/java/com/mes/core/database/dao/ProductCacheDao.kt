package com.mes.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mes.core.database.entity.ProductCacheEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductCacheDao {
    @Query("SELECT * FROM products_cache ORDER BY isFeatured DESC, name ASC")
    fun getAllProducts(): Flow<List<ProductCacheEntity>>

    @Query("SELECT * FROM products_cache WHERE category = :category ORDER BY isFeatured DESC, name ASC")
    fun getProductsByCategory(category: String): Flow<List<ProductCacheEntity>>

    @Query("SELECT * FROM products_cache WHERE id = :productId")
    suspend fun getProductById(productId: String): ProductCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductCacheEntity>)

    @Query("DELETE FROM products_cache")
    suspend fun clearAll()

    @Query("DELETE FROM products_cache WHERE cachedAt < :maxAge")
    suspend fun clearStale(maxAge: Long)
}
