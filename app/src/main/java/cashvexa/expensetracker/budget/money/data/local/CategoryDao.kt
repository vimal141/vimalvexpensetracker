package cashvexa.expensetracker.budget.money.data.local

import androidx.room.*
import cashvexa.expensetracker.budget.money.data.model.Category
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories WHERE type = :type ORDER BY isDefault DESC, id ASC")
    fun getCategoriesByType(type: String): Flow<List<Category>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: Category)

    @Update
    suspend fun update(category: Category)

    @Delete
    suspend fun delete(category: Category)

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun getCategoryCount(): Int

    @Query("SELECT * FROM categories")
    suspend fun getAllCategories(): List<Category>
}
