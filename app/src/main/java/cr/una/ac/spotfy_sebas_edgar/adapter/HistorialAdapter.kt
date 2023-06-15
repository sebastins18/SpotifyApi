package cr.una.ac.spotfy_sebas_edgar.adapter


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


import cr.una.ac.spotfy_sebas_edgar.R
import cr.una.ac.spotfy_sebas_edgar.entity.Historial


class HistorialAdapter(var history: ArrayList<Historial>, var onItemClick: (String) -> Unit) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val VIEW_TYPE_ITEM = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.history_item, parent, false)
        return ViewHolder(view)

    }
    override fun getItemViewType(position: Int): Int {
        return VIEW_TYPE_ITEM
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ViewHolder) {
            holder.bind(history[position])
            holder.itemView.setOnClickListener {
                onItemClick(history[position].song_name)
            }
        }
    }

    override fun getItemCount(): Int {
        return history.size
    }
    fun updateData(newData: ArrayList<Historial>) {
        history = newData
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val trackName_View = itemView.findViewById<TextView>(R.id.history_text)

        fun bind(track: Historial) {

            val trackName = track.song_name
            trackName_View.text = trackName

        }
    }
}