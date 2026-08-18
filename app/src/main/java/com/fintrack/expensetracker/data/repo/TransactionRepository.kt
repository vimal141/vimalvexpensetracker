package com.fintrack.expensetracker.data.repo

import com.fintrack.expensetracker.data.local.TransactionDao
import com.fintrack.expensetracker.data.model.Transaction
import com.fintrack.expensetracker.data.model.CategorySum
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class TransactionRepository(private val dao: TransactionDao) {

    fun getAllTransactions(accountId: Int): Flow<List<Transaction>> =
        dao.getAllTransactions(accountId)

    fun getTransactionsForRange(accountId: Int, start: Long, end: Long): Flow<List<Transaction>> =
        dao.getTransactionsForRange(accountId, start, end)

    fun searchWithFilters(accountId: Int, query: String, type: String, category: String): Flow<List<Transaction>> =
        dao.searchWithFilters(accountId, query, type, category)

    fun getCategoryBreakdown(accountId: Int, start: Long, end: Long): Flow<List<CategorySum>> =
        dao.getCategoryBreakdown(accountId, start, end)

    suspend fun insert(transaction: Transaction) =
        withContext(Dispatchers.IO) { dao.insert(transaction) }
    suspend fun update(transaction: Transaction) =
        withContext(Dispatchers.IO) { dao.update(transaction) }
    suspend fun delete(transaction: Transaction) =
        withContext(Dispatchers.IO) { dao.delete(transaction) }
    suspend fun deleteAll() = withContext(Dispatchers.IO) { dao.deleteAll() }
}