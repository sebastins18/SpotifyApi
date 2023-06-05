package cr.una.ac.spotfy_sebas_edgar

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cr.una.ac.spotfy_sebas_edgar.adapter.HistorialAdapter
import cr.una.ac.spotfy_sebas_edgar.adapter.SpotifyAdapter
import cr.una.ac.spotfy_sebas_edgar.databinding.FragmentFirstBinding
import cr.una.ac.spotfy_sebas_edgar.entity.Historial
import cr.una.ac.spotfy_sebas_edgar.entity.Track
import cr.una.ac.spotfy_sebas_edgar.viewModel.SpotifyViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!

    private lateinit var spotifyViewModel : SpotifyViewModel
    private lateinit var songs : List<Track>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        spotifyViewModel = ViewModelProvider(requireActivity()).get(SpotifyViewModel::class.java)

        binding.buttonSearch.setOnClickListener {
            val query = binding.editTextSearch.text.toString()
            if (query.isNotEmpty()) {
                searchSongs(query)
            } else {
                Toast.makeText(requireContext(), "Por favor ingrese el nombre de una canción", Toast.LENGTH_SHORT).show()
            }
        }

        binding.buttonFirst.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }

        val listView = view.findViewById<RecyclerView>(R.id.recyclerViewSongs)
        songs = mutableListOf<Track>()
        val adapter = SpotifyAdapter(songs as ArrayList<Track>)
        listView.adapter = adapter
        listView.layoutManager = LinearLayoutManager(requireContext())

        spotifyViewModel.songs.observe(viewLifecycleOwner) { elements ->
            adapter.updateData(elements as ArrayList<Track>)
            songs = elements
        }

        val historialView = view.findViewById<RecyclerView>(R.id.recyclerViewHistorial) // Asegúrate de cambiar este ID al que corresponda
        val historial = mutableListOf<Historial>()
        val historialAdapter = HistorialAdapter(historial) // Asegúrate de que tienes un adaptador para el historial
        historialView.adapter = historialAdapter
        historialView.layoutManager = LinearLayoutManager(requireContext())

        binding.editTextSearch.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                spotifyViewModel.historial.observe(viewLifecycleOwner, { elements ->
                    historial.clear()
                    historial.addAll(elements)
                    historialAdapter.notifyDataSetChanged()
                })
            }
        }
    }

    private fun searchSongs(query: String) {
        spotifyViewModel.insertHistorial(query)
        GlobalScope.launch(Dispatchers.Main) {
            spotifyViewModel.searchSongs(query)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}