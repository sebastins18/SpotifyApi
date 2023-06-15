package cr.una.ac.spotfy_sebas_edgar.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Historial (@PrimaryKey(autoGenerate = true) val id: Long?,
                      val song_name: String,val _date: String )



