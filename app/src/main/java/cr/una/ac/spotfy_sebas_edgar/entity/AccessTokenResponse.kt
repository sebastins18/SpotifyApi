package cr.una.ac.spotfy_sebas_edgar.entity

import com.google.gson.annotations.SerializedName

data class AccessTokenResponse(
    @SerializedName("access_token")
    val accessToken: String?
)
