package cr.una.ac.spotfy_sebas_edgar

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import cr.una.ac.spotfy_sebas_edgar.adapter.TopTracksAdapter
import cr.una.ac.spotfy_sebas_edgar.entity.Track
import cr.una.ac.spotfy_sebas_edgar.viewModel.ArtistSearchViewmodel

import android.media.AudioAttributes
import android.media.MediaPlayer



// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "artist"
private const val ARG_PARAM2 = "artist_url"
private const val ARG_PARAM3 = "artist_name"

/**
 * A simple [Fragment] subclass.
 * Use the [ArtistFragment.newInstance] factory method to
 * create an instance of this fragment.
 */

class ArtistFragment : Fragment() {

    private var artistUrl: String? = "null"
    private var artistId: String? = "null"
    private var artistName: String? = "null"

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
            artistId = it.getString(ARG_PARAM1)
            artistUrl = it.getString(ARG_PARAM2)
            artistName = it.getString(ARG_PARAM3)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_artist, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Create a new instance of MediaPlayer
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()
        mediaPlayer.setAudioAttributes(audioAttributes)

        tracks = mutableListOf<Track>()

        val viewModel = ViewModelProvider(this).get(ArtistSearchViewmodel::class.java)
        val listView = view.findViewById<RecyclerView>(R.id.list_view_top)
        val adapter = TopTracksAdapter(tracks as ArrayList<Track>, requireContext()) { selectedItem ->

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

        artistId?.let {
            viewModel.search(it)
            viewModel.getArtist(it)
        }

        val cover = view.findViewById<ImageView>(R.id.image_album)
        val artist = view.findViewById<TextView>(R.id.text_artist_name)
        val progressBar = view.findViewById<ProgressBar>(R.id.loading_progress)

        viewModel.artistImage.observe(viewLifecycleOwner) { imageURL ->
            println(imageURL)
            Glide.with(view).load(imageURL).listener(object: RequestListener<Drawable> {

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

            artistName?.let { cover.contentDescription = artistName }
        }



        artistName?.let { artist.text = it }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopMusic()
    }

}