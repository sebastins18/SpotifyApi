package cr.una.ac.spotfy_sebas_edgar.Repository

import androidx.lifecycle.LiveData
import cr.una.ac.spotfy_sebas_edgar.DAO.HistorialDao
import cr.una.ac.spotfy_sebas_edgar.entity.Historial

class HistorialRepository(private val historialDao: HistorialDao) {
    val historial: LiveData<List<Historial>> = historialDao.getHistorial()

    suspend fun insert(historial: Historial) {
        historialDao.insert(historial)
    }

    suspend fun deleteAll() {
        historialDao.deleteAll()
    }
}
