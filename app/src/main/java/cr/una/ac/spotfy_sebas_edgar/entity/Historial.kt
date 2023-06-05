package cr.una.ac.spotfy_sebas_edgar.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "historial_table")
data class Historial(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    val query: String
)


