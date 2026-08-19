package cashvexa.expensetracker.budget.money.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "accounts")
data class Account(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val accountNumber: String,
    val timestamp: Long = System.currentTimeMillis()
) : Serializable