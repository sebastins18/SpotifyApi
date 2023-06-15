package cr.una.ac.spotfy_sebas_edgar

import android.content.ContentValues.TAG
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cr.una.ac.spotfy_sebas_edgar.adapter.HistorialAdapter
import cr.una.ac.spotfy_sebas_edgar.adapter.SpotifyAdapter

import cr.una.ac.spotfy_sebas_edgar.databinding.FragmentSearchBinding
import cr.una.ac.spotfy_sebas_edgar.entity.Historial
import cr.una.ac.spotfy_sebas_edgar.entity.Track
import cr.una.ac.spotfy_sebas_edgar.viewModel.SpotifyViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class SearchFragment : Fragment(), SpotifyAdapter.OnItemClickListener {

    private var _binding: FragmentSearchBinding? = null
    private lateinit var tracks: List<Track>
    private lateinit var history: List<Historial>

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val mediaPlayer = MediaPlayer()
    private var isPlaying = false

    private fun stopMusic() {
        if (isPlaying) {
            mediaPlayer.stop()
            mediaPlayer.reset()
            isPlaying = false
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root

    }



    override fun onViewAlbumClicked(track: Track) {
        //val albumFragment = AlbumFragment.newInstance(track.album.name)
        //println(track)
        val bundle = Bundle()
        bundle.putString("album", track.album.id)
        findNavController().navigate(R.id.action_searchFragment_to_AlbumFragment, bundle)
    }

    override fun onViewArtistClicked(track: Track) {
        //println(track)
        val bundle = Bundle()
        bundle.putString("artist", track.artists[0].id)
        bundle.putString("artist_url", "https://i.scdn.co/image/ab67616d0000b273e55be22cd0085496fee07b29")
        bundle.putString("artist_name", track.artists[0].name)
        findNavController().navigate(R.id.action_searchFragment_to_ArtistFragment, bundle)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tracks = mutableListOf<Track>()
        history = mutableListOf<Historial>()

        // Create a new instance of MediaPlayer
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()
        mediaPlayer.setAudioAttributes(audioAttributes)

        val viewModel = ViewModelProvider(this).get(SpotifyViewModel::class.java)
        //val playModel = ViewModelProvider(this).get(PlayViewModel::class.java)

        val searchField = view.findViewById<SearchView>(R.id.search_bar)

        val listView = view.findViewById<RecyclerView>(R.id.list_view)
        val historyView = view.findViewById<RecyclerView>(R.id.history_view)

        val adapter = SpotifyAdapter(tracks as ArrayList<Track>, requireContext()) { selectedItem ->

            val previewUrl = selectedItem.preview_url

            if (!previewUrl.isNullOrEmpty()) {
                try {
                    if (isPlaying) {
                        mediaPlayer.stop()
                        mediaPlayer.reset()
                        isPlaying = false
                    } else {
                        mediaPlayer.setDataSource(previewUrl)
                        mediaPlayer.prepareAsync()
                        mediaPlayer.setOnPreparedListener {
                            isPlaying = true
                            mediaPlayer.start()
                        }
                    }
                } catch (e: Exception) {
                    // manejar la excepción aquí, como mostrar un mensaje de error al usuario
                    Log.e(TAG, "Error al reproducir la vista previa: ${e.message}")
                }
            } else {
                // manejar el caso en que la URL de vista previa es nula o está vacía
                Log.w(TAG, "No hay vista previa disponible para esta canción.")
            }
        }


        adapter.onItemClickListener = this

        val historyAdapter = HistorialAdapter(history as ArrayList<Historial>) { selectedItem ->
            searchField.setQuery(selectedItem, false)
        }

        listView.adapter = adapter
        listView.layoutManager = LinearLayoutManager(requireContext())

        historyView.adapter = historyAdapter
        historyView.layoutManager = LinearLayoutManager(requireContext())

        viewModel.tracks.observe(viewLifecycleOwner) { elementos ->
            adapter.updateData(elementos as ArrayList<Track>)
            tracks = elementos
        }

        viewModel.history.observe(viewLifecycleOwner) { elementos ->
            //order the elemenst from the most recent to the oldest
            elementos.sortedByDescending { it.id }
            historyAdapter.updateData(elementos as ArrayList<Historial>)
            history = elementos
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
        }

        //val menuButton = view.findViewById<ImageButton>(R.id.options_button)

        /*
        menuButton.setOnClickListener {
            // Create a PopupMenu

        }

         */

        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {

            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                val position = viewHolder.adapterPosition
                val entity = (history as MutableList<Historial>).get(position)
                searchField.setQuery(entity.song_name, false)

                return false
            }


            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val entity = (history as MutableList<Historial>).get(position)

                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        viewModel.deleteHistoryItem(requireContext(), entity)
                    }
                }

                // Elimina el elemento cuando se detecta el deslizamiento hacia la derecha
                (history as MutableList<Historial>).removeAt(position)
                historyAdapter.updateData(history as ArrayList<Historial>)
            }

            // Sobrescribe el método para dibujar la etiqueta al deslizar
            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                if (viewHolder is HistorialAdapter.ViewHolder) {
                    super.onChildDraw(
                        c,
                        recyclerView,
                        viewHolder,
                        dX,
                        dY,
                        actionState,
                        isCurrentlyActive
                    )
                    if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                        val itemView = viewHolder.itemView
                        val paint = Paint()
                        paint.color = Color.RED
                        val deleteIcon = ContextCompat.getDrawable(
                            requireContext(),
                            android.R.drawable.ic_menu_delete
                        )
                        val iconMargin = (itemView.height - deleteIcon!!.intrinsicHeight) / 2
                        val iconTop =
                            itemView.top + (itemView.height - deleteIcon.intrinsicHeight) / 2
                        val iconBottom = iconTop + deleteIcon.intrinsicHeight

                        // Dibuja el fondo rojo
                        c.drawRect(
                            itemView.left.toFloat(),
                            itemView.top.toFloat(),
                            itemView.right.toFloat(),
                            itemView.bottom.toFloat(),
                            paint
                        )

                        // Calcula las posiciones del icono de eliminar
                        val iconLeft = itemView.right - iconMargin - deleteIcon.intrinsicWidth
                        val iconRight = itemView.right - iconMargin
                        deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom)

                        // Dibuja el icono de eliminar
                        deleteIcon.draw(c)
                    }
                }
            }
        })

        // Adjunta el ItemTouchHelper al RecyclerView
        itemTouchHelper.attachToRecyclerView(historyView)

        //searchField on touch listener
        searchField.setOnQueryTextFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                println("Hi the history is visible")
                val query = searchField.query.toString()
                if (query.length > 5) {
                    lifecycleScope.launch {
                        withContext(Dispatchers.IO) {
                            viewModel.getHistory(requireContext(), query)
                        }
                    }
                    historyView.visibility = View.VISIBLE
                } else {
                    historyView.visibility = View.GONE
                }
            } else {
                println("Hi the history is gone")
                historyView.visibility = View.GONE
            }
        }

        searchField.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String): Boolean {

                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        viewModel.addHistory(requireContext(), query)
                    }
                }

                viewModel.search(query)
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {

                if (newText.length > 5) {
                    lifecycleScope.launch {
                        withContext(Dispatchers.IO) {
                            viewModel.getHistory(requireContext(), newText)
                        }
                    }
                    viewModel.search(newText)
                    historyView.visibility = View.VISIBLE

                } else if(newText.isEmpty()){
                    adapter.updateData(arrayListOf<Track>())
                    historyView.visibility = View.GONE
                } else {
                    historyView.visibility = View.GONE
                }

                return false
            }
        })



    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        stopMusic()
        mediaPlayer.release()
    }



}