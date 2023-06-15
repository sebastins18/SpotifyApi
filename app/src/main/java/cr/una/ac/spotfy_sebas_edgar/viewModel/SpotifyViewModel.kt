package cr.una.ac.spotfy_sebas_edgar.viewModel

import android.content.Context
import android.util.Base64
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

import cr.una.ac.spotfy_sebas_edgar.dao.HistoryDAO
import cr.una.ac.spotfy_sebas_edgar.db.AppDatabase

import cr.una.ac.spotfy_sebas_edgar.entity.AccessTokenResponse
import cr.una.ac.spotfy_sebas_edgar.entity.Album
import cr.una.ac.spotfy_sebas_edgar.entity.Artist
import cr.una.ac.spotfy_sebas_edgar.entity.Cover
import cr.una.ac.spotfy_sebas_edgar.entity.Historial
import cr.una.ac.spotfy_sebas_edgar.entity.Track
import cr.una.ac.spotfy_sebas_edgar.entity.TrackResponse

import cr.una.ac.spotfy_sebas_edgar.service.SpotifyService


import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class SpotifyViewModel() :ViewModel() {

    private var _tracks: MutableLiveData<List<Track>> = MutableLiveData()
    var tracks : LiveData<List<Track>> = _tracks

    private var _history: MutableLiveData<List<Historial>> = MutableLiveData()
    var history : LiveData<List<Historial>> = _history

    private lateinit var historyDAO : HistoryDAO

    private var _errorMessage: MutableLiveData<String> = MutableLiveData()
    var errorMessage: LiveData<String> = _errorMessage

    private var albumURL = ""

    private fun displayErrorMessage(message: String) {
        _errorMessage.value = message
    }

    private val spotifyServiceToken: SpotifyService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://accounts.spotify.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(SpotifyService::class.java)
    }
    private val spotifyService: SpotifyService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.spotify.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(SpotifyService::class.java)
    }

    private fun getAccessToken(): Call<AccessTokenResponse> {
        val clientId = "f13969da015a4f49bb1f1edef2185d4e"
        val clientSecret = "e3077426f4714315937111d5e82cd918"
        val base64Auth = Base64.encodeToString("$clientId:$clientSecret".toByteArray(), Base64.NO_WRAP)

        return spotifyServiceToken.getAccessToken(
            "Basic $base64Auth",
            "client_credentials"
        )
    }

    fun search(query: String){

        val tokenRequest = getAccessToken()
        tokenRequest.enqueue(object : Callback<AccessTokenResponse> {
            override fun onResponse(call: Call<AccessTokenResponse>, response: Response<AccessTokenResponse>) {
                if (response.isSuccessful) {
                    val accessTokenResponse = response.body()
                    val accessToken = accessTokenResponse?.accessToken

                    if (accessToken != null) {

                        val searchRequest = spotifyService.searchTrack("Bearer $accessToken", query)
                        searchRequest.enqueue(object : Callback<TrackResponse> {
                            override fun onResponse(call: Call<TrackResponse>, response: Response<TrackResponse>) {
                                if (response.isSuccessful) {
                                    val trackResponse = response.body()
                                    val trackList = mutableListOf<Track>()



                                    if (trackResponse != null && trackResponse.tracks.items.isNotEmpty()) {

                                        for (track in trackResponse.tracks.items){


                                            val album = track.album
                                            val artists = track.artists
                                            println(artists)

                                            val albumName = album.name
                                            val imageUrl = album.images[0].url
                                            val albumId = album.id

                                            val cover = ArrayList<Cover>()
                                            cover.add(Cover(imageUrl))

                                            val trackObject = Track(
                                                track.name,
                                                Album(albumId, albumName, cover),
                                                artists,
                                                track.uri,
                                                track.popularity
                                            )

                                            trackList.add(trackObject)

                                            //println("Track: " + track.name)

                                        }
                                        _tracks.postValue(trackList)

                                    } else {
                                        displayErrorMessage("No se encontraron canciones.")
                                    }

                                } else {
                                    System.out.println("Mensaje:    "+response.raw())
                                    displayErrorMessage("Error en la respuesta del servidor.")
                                }
                            }

                            override fun onFailure(call: Call<TrackResponse>, t: Throwable) {
                                println(t)
                                displayErrorMessage(t.message ?: "Error en la solicitud de búsqueda.")
                            }
                        })
                    } else {
                        displayErrorMessage("Error al obtener el accessToken.")
                    }
                } else {
                    System.out.println("Mensaje:    "+response.raw())
                    displayErrorMessage("Error en la respuesta del servidor.")
                }
            }

            override fun onFailure(call: Call<AccessTokenResponse>, t: Throwable) {
                displayErrorMessage("Error en la solicitud de accessToken.")
            }
        })
    }

    fun addHistory(context: Context, query: String) {
        initDatabase(context)
        val current = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("d MMMM, yyyy")
        val date = current.format(formatter)
        historyDAO.insert(Historial(null, query, date))
    }


    fun deleteHistoryItem(context: Context, entity: Historial){
        initDatabase(context)
        historyDAO.delete(entity)
    }

    fun getHistory(context: Context, text: String){
        initDatabase(context)
        _history.postValue(historyDAO.typeHistory(text))
    }

    private fun initDatabase(context: Context) {
        historyDAO = AppDatabase.getInstance(context).historyDao()
    }
    val artistImageUrl: MutableLiveData<String> = MutableLiveData()

    fun getArtist(id: String){

        val tokenRequest = getAccessToken()
        tokenRequest.enqueue(object : Callback<AccessTokenResponse> {
            override fun onResponse(call: Call<AccessTokenResponse>, response: Response<AccessTokenResponse>) {
                if (response.isSuccessful) {
                    val accessTokenResponse = response.body()
                    val accessToken = accessTokenResponse?.accessToken

                    if (accessToken != null) {

                        val artistRequest = spotifyService.getArtist("Bearer $accessToken", id)
                        artistRequest.enqueue(object : Callback<Artist> {
                            override fun onResponse(call: Call<Artist>, response: Response<Artist>) {
                                if (response.isSuccessful) {
                                    val artistResponse = response.body()


                                    val imageUrl = artistResponse?.images?.get(0)?.url


                                    artistImageUrl.postValue(imageUrl ?: "")
                                } else {
                                    System.out.println("Mensaje:    "+response.raw())
                                    displayErrorMessage("Error en la respuesta del servidor.")
                                }
                            }

                            override fun onFailure(call: Call<Artist>, t: Throwable) {
                                println(t)
                                displayErrorMessage(t.message ?: "Error en la solicitud del artista.")
                            }
                        })
                    } else {
                        displayErrorMessage("Error al obtener el accessToken.")
                    }
                } else {
                    System.out.println("Mensaje:    "+response.raw())
                    displayErrorMessage("Error en la respuesta del servidor.")
                }
            }

            override fun onFailure(call: Call<AccessTokenResponse>, t: Throwable) {
                displayErrorMessage("Error en la solicitud de accessToken.")
            }
        })
    }
}



/*

    private fun displayTrackInfo(trackName: String, artistName: String) {
        val message = "Canción encontrada: $trackName - $artistName"

    }



 */