package cr.una.ac.spotfy_sebas_edgar.DAO

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query // Use androidx.room.Query, not retrofit2.http.Query
import cr.una.ac.spotfy_sebas_edgar.entity.Historial

@Dao
interface HistorialDao {
    @Query("SELECT * FROM historial_table ORDER BY id DESC")
    fun getHistorial(): LiveData<List<Historial>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(historial: Historial)

    @Query("DELETE FROM historial_table")
    suspend fun deleteAll()
}
