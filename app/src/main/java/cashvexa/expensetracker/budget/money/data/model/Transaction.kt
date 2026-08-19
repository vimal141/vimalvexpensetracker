package cashvexa.expensetracker.budget.money.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val amount: Double,
    val type: String,
    val category: String,
    val date: Long,
    val accountId: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String? = null
) : Serializable