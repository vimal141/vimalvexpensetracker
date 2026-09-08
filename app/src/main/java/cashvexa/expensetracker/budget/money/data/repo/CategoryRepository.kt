package cashvexa.expensetracker.budget.money.data.repo

import cashvexa.expensetracker.budget.money.data.local.CategoryDao
import cashvexa.expensetracker.budget.money.data.model.Category
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
