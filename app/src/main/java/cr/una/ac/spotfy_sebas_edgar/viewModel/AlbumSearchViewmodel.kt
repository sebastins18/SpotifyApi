package cr.una.ac.spotfy_sebas_edgar.viewModel

import android.util.Base64
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel


import cr.una.ac.spotfy_sebas_edgar.entity.AccessTokenResponse
import cr.una.ac.spotfy_sebas_edgar.entity.Album
import cr.una.ac.spotfy_sebas_edgar.entity.AlbumResponse
import cr.una.ac.spotfy_sebas_edgar.entity.Cover
import cr.una.ac.spotfy_sebas_edgar.entity.Track
import cr.una.ac.spotfy_sebas_edgar.service.SpotifyService

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AlbumSearchViewmodel: ViewModel() {

    private var _tracks: MutableLiveData<List<Track>> = MutableLiveData()
    var tracks : LiveData<List<Track>> = _tracks

    private var _errorMessage: MutableLiveData<String> = MutableLiveData()
    var errorMessage: LiveData<String> = _errorMessage

    private var _album: MutableLiveData<AlbumResponse> = MutableLiveData()
    var album : MutableLiveData<AlbumResponse> = _album

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

    fun searchAlbum(query: String){
        val tockenResponse = getAccessToken()
        tockenResponse.enqueue(object : Callback<AccessTokenResponse> {
            override fun onResponse(call: Call<AccessTokenResponse>, response: Response<AccessTokenResponse>) {
                if (response.isSuccessful) {
                    val accessTokenResponse = response.body()
                    val accessToken = accessTokenResponse?.accessToken

                    if (accessToken != null) {

                        val searchRequest = spotifyService.searchAlbum("Bearer $accessToken", query)
                        searchRequest.enqueue(object : Callback<AlbumResponse> {
                            override fun onResponse(call: Call<AlbumResponse>, response: Response<AlbumResponse>) {
                                if (response.isSuccessful) {
                                    val albumResponse = response.body()
                                    val trackList = mutableListOf<Track>()

                                    if (albumResponse != null) {
                                        val albumURL = albumResponse.images[0].url
                                        val albumName = albumResponse.name

                                        if (albumResponse.tracks.items.isNotEmpty()) {

                                            for (track in albumResponse.tracks.items) {

                                                // Create a Track object and populate its properties
                                                val artists = track.artists

                                                val cover = ArrayList<Cover>()
                                                cover.add(Cover(albumURL))

                                                var previewURL = track.preview_url

                                                if(track.preview_url == null){
                                                    previewURL = ""
                                                    displayErrorMessage("Algunas canciones no tienen demos")
                                                }

                                                val trackObject = Track(
                                                    track.id,
                                                    track.name,
                                                    Album("", albumName, cover),
                                                    artists,
                                                    track.uri,
                                                    previewURL,
                                                    track.popularity
                                                )
                                                trackList.add(trackObject)
                                            }
                                            _tracks.postValue(trackList)

                                        } else {
                                            displayErrorMessage("Este album no tiene canciones")
                                        }

                                        _album.postValue(albumResponse!!)

                                    } else {
                                        displayErrorMessage("No se encontraron resultados")
                                    }

                                } else {

                                    displayErrorMessage("Error en la respuesta")
                                }
                            }

                            override fun onFailure(call: Call<AlbumResponse>, t: Throwable) {
                                println(t.message)
                                displayErrorMessage("Error en la respuesta")
                            }
                        })

                    } else {
                        displayErrorMessage("Error en la respuesta")
                    }
                } else {
                    displayErrorMessage("Error en la respuesta")
                }
            }

            override fun onFailure(call: Call<AccessTokenResponse>, t: Throwable) {
                displayErrorMessage("Error en la respuesta")
            }
        })
    }

}