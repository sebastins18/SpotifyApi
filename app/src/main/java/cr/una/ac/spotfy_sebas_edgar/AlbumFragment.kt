package cr.una.ac.spotfy_sebas_edgar

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast

import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import cr.una.ac.spotfy_sebas_edgar.adapter.SpotifyAdapter
import cr.una.ac.spotfy_sebas_edgar.entity.Track
import cr.una.ac.spotfy_sebas_edgar.viewModel.AlbumSearchViewmodel


private const val ARG_PARAM1 = "album"


class AlbumFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var album: String? = null
    private lateinit var tracks: List<Track>

    private val mediaPlayer = MediaPlayer()
    private var isPlaying = false

    private fun stopMusic() {
        if (isPlaying) {
            mediaPlayer.stop()
            mediaPlayer.reset()
            isPlaying = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            album = it.getString(ARG_PARAM1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_album, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tracks = mutableListOf<Track>()

        // Create a new instance of MediaPlayer
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()
        mediaPlayer.setAudioAttributes(audioAttributes)

        val viewModel = ViewModelProvider(this).get(AlbumSearchViewmodel::class.java)

        val listView = view.findViewById<RecyclerView>(R.id.recycler_songs)
        val adapter = SpotifyAdapter(tracks as ArrayList<Track>, requireContext()) { selectedItem ->

            val previewUrl = selectedItem.preview_url

            // Set a listener for when the media player is prepared
            if (isPlaying) {
                // Stop playing the demo
                mediaPlayer.stop()
                mediaPlayer.reset()
                isPlaying = false

            }else{

                if(previewUrl.isNotEmpty()){
                    // Set the data source to the previewUrl
                    mediaPlayer.setDataSource(previewUrl)

                    // Prepare the media player asynchronously
                    mediaPlayer.prepareAsync()

                    mediaPlayer.setOnPreparedListener {
                        // Start playing the demo
                        isPlaying = true
                        mediaPlayer.start()
                    }
                }else{
                    Toast.makeText(requireContext(), "Esta cancion no tiene demo", Toast.LENGTH_SHORT).show()
                }

            }

        }

        listView.adapter = adapter
        listView.layoutManager = LinearLayoutManager(requireContext())

        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
        }
        viewModel.tracks.observe(viewLifecycleOwner) { elementos ->
            adapter.updateData(elementos as ArrayList<Track>)
            tracks = elementos
        }

        album?.let {
            viewModel.searchAlbum(it) }

        val cover = view.findViewById<ImageView>(R.id.image_album)
        val name = view.findViewById<TextView>(R.id.text_album_name)
        val artist = view.findViewById<TextView>(R.id.text_artist_name)
        val progressBar = view.findViewById<ProgressBar>(R.id.loading_progress)
        val genres = view.findViewById<TextView>(R.id.text_genres)
        val releasedDate = view.findViewById<TextView>(R.id.ReleasedDate)

        progressBar.visibility = View.VISIBLE

        viewModel.album.observe(viewLifecycleOwner) { album ->

            name.text = album.name
            releasedDate.text = album.release_date

            val artistsBuilder = StringBuilder()
            for ((index, _artist) in album.artists.withIndex()) {
                artistsBuilder.append(_artist.name)
                if (index < album.artists.size - 1) {
                    artistsBuilder.append(", ")
                }
            }
            artist.text = artistsBuilder.toString()

            val genreBuilder = StringBuilder()
            for ((index, genre) in album.genres.withIndex()) {
                genreBuilder.append(genre)
                if (index < album.genres.size - 1) {
                    genreBuilder.append(" - ")
                }
            }
            genres.text = genreBuilder.toString()

            if (genres.text == "") {
                genres.text = "Rock, Rock Español, Classic Rock"
            }

            Glide.with(view).load(album.images[0].url).listener(object: RequestListener<Drawable> {

                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    progressBar.visibility = View.GONE
                    return false
                }

                override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                    progressBar.visibility = View.GONE
                    return false
                }

            }).error(R.drawable.ic_launcher_foreground).into(cover)

            cover.contentDescription = album.name


        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopMusic()
    }

}