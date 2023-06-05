package cr.una.ac.spotfy_sebas_edgar.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import cr.una.ac.spotfy_sebas_edgar.R
import cr.una.ac.spotfy_sebas_edgar.entity.Track

class SpotifyAdapter(private var songs: ArrayList<Track>): RecyclerView.Adapter<SpotifyAdapter.SongViewHolder>(){

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.song_item, parent, false)
            return SongViewHolder(view)
        }

        override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
            val song = songs[position]
            holder.bind(song)
        }

        override fun getItemCount(): Int {
            return songs.size
        }

        fun updateData(newData: ArrayList<Track>) {
            songs = newData
            notifyDataSetChanged()
        }

        inner class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val songNameTextView = itemView.findViewById<TextView>(R.id.song_name)
            private val albumNameTextView = itemView.findViewById<TextView>(R.id.album_name)
            private val artistsTextView = itemView.findViewById<TextView>(R.id.artists)
            private val albumImageView = itemView.findViewById<ImageView>(R.id.album_image)


            fun bind(song: Track) {
                songNameTextView.text = song.name
                albumNameTextView.text = song.album.name

                var artistas = ""
                song.artists.forEach {
                    artistas += it.name + ", "
                }
                //quito la ultima coma
                artistas = artistas.substring(0, artistas.length - 2)

                artistsTextView.text = artistas




                Picasso.get()
                    .load(song.album.images[0].url)
                    .into(albumImageView)
            }
        }
    }