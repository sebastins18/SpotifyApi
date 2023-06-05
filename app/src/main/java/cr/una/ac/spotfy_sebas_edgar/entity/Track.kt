package cr.una.ac.spotfy_sebas_edgar.entity

data class Track(
    val name: String,
    val album: Album,
    val uri: String,
    val artists : List<Artist>
)