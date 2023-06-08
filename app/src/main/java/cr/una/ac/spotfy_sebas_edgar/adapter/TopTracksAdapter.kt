package cr.una.ac.spotfy_sebas_edgar.adapter

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import cr.una.ac.spotfy_sebas_edgar.R
import cr.una.ac.spotfy_sebas_edgar.entity.Track

class TopTracksAdapter(var tracks: ArrayList<Track>, var context: android.content.Context,
                       var onItemClick: (Track) -> Unit) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val VIEW_TYPE_ITEM = 0
    //afectara?

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_top_tracks, parent, false)
        return ViewHolder(view)

    }
    override fun getItemViewType(position: Int): Int {
        return VIEW_TYPE_ITEM
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ViewHolder) {
            holder.bind(tracks[position])
            holder.itemView.setOnClickListener {
                onItemClick(tracks[position])
            }
        }
    }

    override fun getItemCount(): Int {
        return tracks.size
    }
    fun updateData(newData: ArrayList<Track>) {
        tracks = newData
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val trackName_View = itemView.findViewById<TextView>(R.id.song_title)
        val albumImage_View = itemView.findViewById<ImageView>(R.id.song_image)
        val albumName_View = itemView.findViewById<TextView>(R.id.album_title)
        val popularity_View = itemView.findViewById<TextView>(R.id.popularity)
        val artistName_View = itemView.findViewById<TextView>(R.id.artist_name)
        val loadingWheel = itemView.findViewById<ProgressBar>(R.id.loading_progress)

        fun bind(track: Track) {

            loadingWheel.visibility = View.VISIBLE

            val trackName = track.name
            val albumImageURL = track.album.images[0].url
            val albumName = track.album.name

            trackName_View.text = trackName
            albumName_View.text = albumName
            popularity_View.text = track.popularity.toString()

            val artistsBuilder = StringBuilder()
            for ((index, _artist) in track.artists.withIndex()) {
                artistsBuilder.append(_artist.name)
                if (index < track.artists.size - 1) {
                    artistsBuilder.append(", ")
                }
            }
            artistName_View.text = artistsBuilder.toString()

            Glide.with(itemView).load(albumImageURL).listener(object: RequestListener<Drawable> {

                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    loadingWheel.visibility = View.GONE
                    return false
                }

                override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                    loadingWheel.visibility = View.GONE
                    return false
                }

            }).error(R.drawable.ic_launcher_foreground).into(albumImage_View)

            albumImage_View.contentDescription = albumName

        }
    }
}