package cr.una.ac.spotfy_sebas_edgar.viewModel

import cr.una.ac.spotfy_sebas_edgar.entity.ArtistResponse

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

import android.util.Base64
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import cr.una.ac.spotfy_sebas_edgar.entity.AccessTokenResponse
import cr.una.ac.spotfy_sebas_edgar.entity.Album
import cr.una.ac.spotfy_sebas_edgar.entity.Artist
import cr.una.ac.spotfy_sebas_edgar.entity.Cover
import cr.una.ac.spotfy_sebas_edgar.entity.Track
import cr.una.ac.spotfy_sebas_edgar.service.SpotifyService

class ArtistSearchViewmodel: ViewModel() {

    private var _artistImageUrl: MutableLiveData<String> = MutableLiveData()
    val artistImage: LiveData<String> = _artistImageUrl

    private var _tracks: MutableLiveData<List<Track>> = MutableLiveData()
    var tracks : LiveData<List<Track>> = _tracks

    private var _errorMessage: MutableLiveData<String> = MutableLiveData()
    var errorMessage: LiveData<String> = _errorMessage

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

                                    // Aquí tendrías la url de la imagen del artista
                                    val imageUrl = artistResponse?.images?.get(0)?.url

                                    // Actualiza el valor de artistImageUrl
                                    _artistImageUrl.postValue(imageUrl ?: "")
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

    fun search(query: String){

        val tokenRequest = getAccessToken()
        tokenRequest.enqueue(object : Callback<AccessTokenResponse> {
            override fun onResponse(call: Call<AccessTokenResponse>, response: Response<AccessTokenResponse>) {
                if (response.isSuccessful) {
                    val accessTokenResponse = response.body()
                    val accessToken = accessTokenResponse?.accessToken

                    if (accessToken != null) {

                        val searchRequest = spotifyService.searchTopTracks("Bearer $accessToken", query)
                        searchRequest.enqueue(object : Callback<ArtistResponse> {
                            override fun onResponse(call: Call<ArtistResponse>, response: Response<ArtistResponse>) {

                                if (response.isSuccessful) {
                                    val trackResponse = response.body()
                                    val trackList = mutableListOf<Track>()

                                    if (trackResponse != null && trackResponse.tracks.isNotEmpty()) {
                                        for (track in trackResponse.tracks){

                                            // Create a Track object and populate its properties
                                            val album = track.album
                                            val artists = track.artists

                                            val albumName = album.name
                                            val imageUrl = album.images[0].url
                                            val albumId = album.id

                                            var previewURL = track.preview_url

                                            if(previewURL == null){
                                                previewURL = ""
                                                displayErrorMessage("Algunas canciones no tienen demos")
                                            }

                                            val cover = ArrayList<Cover>()
                                            cover.add(Cover(imageUrl))

                                            val trackObject = Track(
                                                track.id,
                                                track.name,
                                                Album(albumId, albumName, cover),
                                                artists,
                                                track.uri,
                                                previewURL,
                                                track.popularity
                                            )

                                            trackList.add(trackObject)
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

                            override fun onFailure(call: Call<ArtistResponse>, t: Throwable) {
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

}