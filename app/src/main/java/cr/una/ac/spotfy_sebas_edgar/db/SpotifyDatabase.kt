package cr.una.ac.spotfy_sebas_edgar.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import cr.una.ac.spotfy_sebas_edgar.DAO.HistorialDao
import cr.una.ac.spotfy_sebas_edgar.entity.Historial
import kotlinx.coroutines.CoroutineScope

@Database(entities = [Historial::class], version = 1)
abstract class SpotifyDatabase : RoomDatabase() {
    abstract fun historialDao(): HistorialDao

    companion object {
        @Volatile
        private var INSTANCE: SpotifyDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): SpotifyDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SpotifyDatabase::class.java,
                    "spotify_database"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}