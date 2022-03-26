package com.siddydevelops.spotifykotlin.adapters

import androidx.recyclerview.widget.AsyncListDiffer
import com.siddydevelops.spotifykotlin.R
import com.siddydevelops.spotifykotlin.data.entities.Song
import kotlinx.android.synthetic.main.list_item.view.tvPrimary

class SwipeSongAdapter : BaseSongAdapter(R.layout.list_item) {

    override val differ: AsyncListDiffer<Song> = AsyncListDiffer(this, diffCallback)

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songs[position]
        holder.itemView.apply {

            val text = "${song.title} - ${song.subtitle}"
            tvPrimary.text = text

            setOnClickListener {
                onItemClickListener?.let { click ->
                    click(song)
                }
            }
        }
    }


}