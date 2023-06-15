package cr.una.ac.spotfy_sebas_edgar.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import cr.una.ac.spotfy_sebas_edgar.entity.Artist
import cr.una.ac.spotfy_sebas_edgar.entity.Historial
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path


@Dao
interface HistoryDAO {
    @Insert
    fun insert(entity: Historial)

    @Delete
    fun delete(entity: Historial)

    @Query("SELECT * FROM Historial")
    fun getAll(): List<Historial?>?

    @Query("SELECT * FROM Historial WHERE song_name LIKE '%' || :song_name || '%'")
    fun typeHistory(song_name: String): List<Historial>?

    }
