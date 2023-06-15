package cr.una.ac.spotfy_sebas_edgar.service


import cr.una.ac.spotfy_sebas_edgar.entity.AccessTokenResponse
import cr.una.ac.spotfy_sebas_edgar.entity.AlbumResponse
import cr.una.ac.spotfy_sebas_edgar.entity.Artist
import cr.una.ac.spotfy_sebas_edgar.entity.ArtistResponse
import cr.una.ac.spotfy_sebas_edgar.entity.TrackResponse
import retrofit2.Call
import retrofit2.http.*

interface SpotifyService {
    @FormUrlEncoded
    @POST("api/token")
    fun getAccessToken(
        @Header("Authorization") authorization: String,
        @Field("grant_type") grantType: String
    ): Call<AccessTokenResponse>

    @GET("v1/search?type=track")
    fun searchTrack(
        @Header("Authorization") authorization: String,
        @Query("q") query: String
    ): Call<TrackResponse>

    @GET("v1/albums/{id}")
    fun searchAlbum(
        @Header("Authorization") authorization: String,
        @Path("id") albumId: String
    ): Call<AlbumResponse>

    @GET("v1/artists/{id}/top-tracks?market=ES")
    fun searchTopTracks(
        @Header("Authorization") authorization: String,
        @Path("id") artistId: String,
    ): Call<ArtistResponse>

    @GET("v1/artists/{id}")
    fun getArtist(@Header("Authorization") authHeader: String, @Path("id") id: String): Call<Artist>

}
