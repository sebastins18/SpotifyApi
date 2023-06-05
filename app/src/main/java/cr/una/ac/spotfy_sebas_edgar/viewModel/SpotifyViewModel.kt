package cr.una.ac.spotfy_sebas_edgar.viewModel

import android.app.Application
import android.util.Base64
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cr.una.ac.spotfy_sebas_edgar.Repository.HistorialRepository
import cr.una.ac.spotfy_sebas_edgar.db.SpotifyDatabase
import cr.una.ac.spotfy_sebas_edgar.entity.AccessTokenResponse
import cr.una.ac.spotfy_sebas_edgar.entity.Historial
import cr.una.ac.spotfy_sebas_edgar.entity.Track
import cr.una.ac.spotfy_sebas_edgar.entity.TrackResponse

import cr.una.ac.spotfy_sebas_edgar.service.SpotifyService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SpotifyViewModel(application: Application) : ViewModel() {
    private var _songs: MutableLiveData<List<Track>> = MutableLiveData()
    var songs: LiveData<List<Track>> = _songs

    private val historialDao = SpotifyDatabase.getDatabase(application, viewModelScope).historialDao()
    private val historialRepository = HistorialRepository(historialDao)
    val historial: LiveData<List<Historial>> = historialRepository.historial

    private lateinit var apiService: SpotifyService
    private lateinit var spotifyServiceToken: SpotifyService

    private val clientId = "f13969da015a4f49bb1f1edef2185d4e"
    private val clientSecret = "e3077426f4714315937111d5e82cd918"
    private val base64Auth = Base64.encodeToString("$clientId:$clientSecret".toByteArray(), Base64.NO_WRAP)

    fun searchSongs(query: String) {
        initService()

        val tokenRequest = spotifyServiceToken.getAccessToken(
            "Basic $base64Auth",
            "client_credentials"
        )

        tokenRequest.enqueue(object : Callback<AccessTokenResponse> {
            override fun onResponse(call: Call<AccessTokenResponse>, response: Response<AccessTokenResponse>) {
                if (response.isSuccessful) {
                    val accessTokenResponse = response.body()
                    val accessToken = accessTokenResponse?.accessToken

                    if (accessToken != null) {

                        val searchRequest = apiService.searchSong("Bearer $accessToken", query)
                        searchRequest.enqueue(object : Callback<TrackResponse> {
                            override fun onResponse(call: Call<TrackResponse>, response: Response<TrackResponse>) {
                                if (response.isSuccessful) {
                                    val trackResponse = response.body()
                                    Log.d("SpotifyViewModel", "Response: $trackResponse")
                                    if (trackResponse != null && trackResponse.tracks.items.isNotEmpty()) {
                                        _songs.postValue(trackResponse.tracks.items)
                                        _songs.value = trackResponse.tracks.items
                                    } else {
                                        // Si el listado de canciones es nulo
                                        Log.e("SpotifyViewModel", "Error al obtener las canciones. La lista de canciones es nula.")
                                    }
                                } else {
                                    // Si la respuesta del servidor no fue exitosa
                                    Log.e("SpotifyViewModel", "Error en la respuesta del servidor al obtener las canciones.")
                                }
                            }

                            override fun onFailure(call: Call<TrackResponse>, t: Throwable) {
                                Log.e("SpotifyViewModel", "Error en la solicitud de búsqueda: ${t.message}")
                            }
                        })
                    } else {
                        Log.e("SpotifyViewModel", "El accessToken es null.")
                    }
                } else {
                    Log.e("SpotifyViewModel", "La respuesta del servidor no fue exitosa al obtener el accessToken.")
                }
            }

            override fun onFailure(call: Call<AccessTokenResponse>, t: Throwable) {
                Log.e("SpotifyViewModel", "Error en la solicitud de accessToken: ${t.message}")
            }
        })
    }


    private fun initService() {
        val retrofitToken = Retrofit.Builder()
            .baseUrl("https://accounts.spotify.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        spotifyServiceToken = retrofitToken.create(SpotifyService::class.java)

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.spotify.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(SpotifyService::class.java)
    }

     fun insertHistorial(query: String) {
        viewModelScope.launch(Dispatchers.IO) {
            historialRepository.insert(Historial(query = query))
        }
    }


    fun deleteAllHistorial() {
        viewModelScope.launch(Dispatchers.IO) {
            historialRepository.deleteAll()
        }
    }
}