package com.tb.example.expensebook.data.repo

import com.tb.example.expensebook.data.local.CategoryDao
import com.tb.example.expensebook.data.model.Category
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class CategoryRepository(private val dao: CategoryDao) {
    fun getCategoriesByType(type: String): Flow<List<Category>> = dao.getCategoriesByType(type)
    
    suspend fun insert(category: Category) = withContext(Dispatchers.IO) { dao.insert(category) }
    suspend fun update(category: Category) = withContext(Dispatchers.IO) { dao.update(category) }
    suspend fun delete(category: Category) = withContext(Dispatchers.IO) { dao.delete(category) }
    suspend fun getCategoryCount() = withContext(Dispatchers.IO) { dao.getCategoryCount() }
    suspend fun getAllCategories() = withContext(Dispatchers.IO) { dao.getAllCategories() }
}
