package com.tb.example.expensebook.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tb.example.expensebook.data.model.Account
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

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
