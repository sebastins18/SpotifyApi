package cr.una.ac.spotfy_sebas_edgar.service


import cr.una.ac.spotfy_sebas_edgar.entity.AccessTokenResponse
import cr.una.ac.spotfy_sebas_edgar.entity.TrackResponse
import retrofit2.Call
import retrofit2.http.*

interface SpotifyService {
    @POST("api/token")
    @FormUrlEncoded
    fun getAccessToken(
        @Header("Authorization") authHeader: String,
        @Field("grant_type") grantType: String = "client_credentials"
    ): Call<AccessTokenResponse>

    @GET("v1/search")
    fun searchSong(
        @Header("Authorization") authHeader: String,
        @Query("q") query: String,
        @Query("type") type: String = "track",
        @Query("limit") limit: Int = 10
    ): Call<TrackResponse>
}
