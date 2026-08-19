package cashvexa.expensetracker.budget.money.data.local

import androidx.room.*
import cashvexa.expensetracker.budget.money.data.model.CategorySum
import cashvexa.expensetracker.budget.money.data.model.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
     fun insert(transaction: Transaction)

    @Update
     fun update(transaction: Transaction)

    @Delete
     fun delete(transaction: Transaction)

    @Query("DELETE FROM transactions")
     fun deleteAll()

    @Query("SELECT * FROM transactions WHERE accountId = :accountId ORDER BY date DESC, timestamp DESC")
    fun getAllTransactions(accountId: Int): Flow<List<Transaction>>


    @Query("SELECT * FROM transactions WHERE accountId = :accountId AND date BETWEEN :start AND :end ORDER BY date DESC, timestamp DESC")
    fun getTransactionsForRange(accountId: Int, start: Long, end: Long): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE accountId = :accountId AND type = :type ORDER BY date DESC")
    fun getTransactionsByType(accountId: Int, type: String): Flow<List<Transaction>>

    @Query("""
        SELECT * FROM transactions
        WHERE accountId = :accountId AND (title LIKE '%' || :query || '%'
            OR category LIKE '%' || :query || '%'
            OR notes LIKE '%' || :query || '%')
        ORDER BY date DESC
    """)
    fun searchTransactions(accountId: Int, query: String): Flow<List<Transaction>>

    @Query("""
        SELECT * FROM transactions
        WHERE accountId = :accountId AND (title LIKE '%' || :query || '%'
            OR category LIKE '%' || :query || '%'
            OR notes LIKE '%' || :query || '%')
        AND (:type = 'All' OR type = :type)
        AND (:category = 'All' OR category = :category)
        ORDER BY date DESC
    """)
    fun searchWithFilters(accountId: Int, query: String, type: String, category: String): Flow<List<Transaction>>

    @Query("SELECT SUM(amount) FROM transactions WHERE accountId = :accountId AND type = 'Income' AND date BETWEEN :start AND :end")
    fun getTotalIncomeForRange(accountId: Int, start: Long, end: Long): Flow<Double?>

    @Query("SELECT SUM(amount) FROM transactions WHERE accountId = :accountId AND type = 'Expense' AND date BETWEEN :start AND :end")
    fun getTotalExpenseForRange(accountId: Int, start: Long, end: Long): Flow<Double?>

    @Query("""
        SELECT category, SUM(amount) as total
        FROM transactions
        WHERE accountId = :accountId AND type = 'Expense' AND date BETWEEN :start AND :end
        GROUP BY category
        ORDER BY total DESC
    """)
    fun getCategoryBreakdown(accountId: Int, start: Long, end: Long): Flow<List<CategorySum>>

    @Query("SELECT DISTINCT category FROM transactions WHERE accountId = :accountId AND type = :type ORDER BY category ASC")
     fun getCategoriesForType(accountId: Int, type: String): List<String>
}

