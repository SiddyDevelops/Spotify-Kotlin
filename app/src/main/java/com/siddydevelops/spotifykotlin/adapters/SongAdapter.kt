package com.siddydevelops.spotifykotlin.adapters


import androidx.recyclerview.widget.AsyncListDiffer
import com.bumptech.glide.RequestManager
import com.siddydevelops.spotifykotlin.R
import com.siddydevelops.spotifykotlin.data.entities.Song
import kotlinx.android.synthetic.main.list_item.view.*
import javax.inject.Inject

class SongAdapter @Inject constructor(
    private val glide: RequestManager
) : BaseSongAdapter(R.layout.list_item) {

    override val differ: AsyncListDiffer<Song> = AsyncListDiffer(this,diffCallback)

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songs[position]
        holder.itemView.apply {
            tvPrimary.text = song.title
            tvSecondary.text = song.subtitle
            ivItemImage.clipToOutline = true
            glide.load(song.imageURL).into(ivItemImage)
            setOnClickListener{
                onItemClickListener?.let { click->
                    click(song)
                }
            }
        }
    }


}