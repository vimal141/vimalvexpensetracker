package cashvexa.expensetracker.budget.money.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cashvexa.expensetracker.budget.money.data.model.Account
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAccount(account: Account): Long

    @Query("SELECT * FROM accounts ORDER BY timestamp ASC")
    fun getAllAccounts(): Flow<List<Account>>

    @Query("SELECT * FROM accounts WHERE id = :accountId")
    fun getAccountById(accountId: Int): Account?

    @Query("SELECT * FROM accounts WHERE name = :name LIMIT 1")
    fun getAccountByName(name: String): Account?
}
