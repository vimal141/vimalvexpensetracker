package cashvexa.expensetracker.budget.money.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val iconName: String,
    val type: String,
    val isDefault: Boolean = false
) : Serializable