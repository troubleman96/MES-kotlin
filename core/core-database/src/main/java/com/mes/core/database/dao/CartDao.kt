package com.mes.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mes.core.database.entity.CartLineEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CartDao {
    @Query("SELECT * FROM cart_lines ORDER BY addedAt DESC")
    fun getAllLines(): Flow<List<CartLineEntity>>

    @Query("SELECT COUNT(*) FROM cart_lines")
    fun getLineCount(): Flow<Int>

    @Query("SELECT SUM(dailyRateTzs * CAST((julianday(rentalEndDate) - julianday(rentalStartDate) + 1) AS INTEGER) * quantity) FROM cart_lines")
    fun getGrandTotal(): Flow<Long?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLine(line: CartLineEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLines(lines: List<CartLineEntity>)

    @Update
    suspend fun updateLine(line: CartLineEntity)

    @Query("DELETE FROM cart_lines WHERE id = :lineId")
    suspend fun deleteLine(lineId: String)

    @Query("DELETE FROM cart_lines")
    suspend fun clearAll()

    @Query("SELECT * FROM cart_lines WHERE id = :lineId")
    suspend fun getLineById(lineId: String): CartLineEntity?
}
