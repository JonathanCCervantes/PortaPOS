package com.jonathan.portapos.data.dao

import androidx.room.*
import com.jonathan.portapos.data.model.Product
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    @Query("SELECT * FROM products ORDER BY category, name")
    fun getAllProducts(): Flow<List<Product>>
    // Flow means: "keep watching and notify me whenever this list changes"

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: Int): Product?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product)

    @Update
    suspend fun updateProduct(product: Product)

    @Delete
    suspend fun deleteProduct(product: Product)

    @Query("SELECT DISTINCT category FROM products ORDER BY category")
    fun getAllCategories(): Flow<List<String>>
}
