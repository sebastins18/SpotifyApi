package cr.una.ac.spotfy_sebas_edgar.entity

data class Track(
    val name: String,
    val album: Album,
    val artists: ArrayList<Artist>,
    val uri: String,
    val popularity: Int
)