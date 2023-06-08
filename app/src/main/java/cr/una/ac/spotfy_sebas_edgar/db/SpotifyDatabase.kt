package cr.una.ac.spotfy_sebas_edgar.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

import cr.una.ac.spotfy_sebas_edgar.dao.HistoryDAO
import cr.una.ac.spotfy_sebas_edgar.converters.Converters
import cr.una.ac.spotfy_sebas_edgar.entity.Historial


@Database(entities = [Historial::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDAO

    companion object {
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            if (instance == null) {
                synchronized(AppDatabase::class) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "history-database"
                    ).build()
                }
            }
            return instance!!
        }
    }
}