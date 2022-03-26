package com.siddydevelops.spotifykotlin.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import com.bumptech.glide.RequestManager
import com.siddydevelops.spotifykotlin.R
import com.siddydevelops.spotifykotlin.adapters.SwipeSongAdapter
import com.siddydevelops.spotifykotlin.data.entities.Song
import com.siddydevelops.spotifykotlin.exoplayer.toSong
import com.siddydevelops.spotifykotlin.other.Status
import com.siddydevelops.spotifykotlin.ui.viewModels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    @Inject
    lateinit var swipeSongAdapter: SwipeSongAdapter

    @Inject
    lateinit var glide: RequestManager

    private var curPLayingSong: Song? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        vpSong.adapter = swipeSongAdapter
        subscribeToObservers()
    }

    private fun switchViewPagerToCurrentSong(song: Song) {
        val newItemIndex = swipeSongAdapter.songs.indexOf(song)
        if(newItemIndex != -1) {
            vpSong.currentItem = newItemIndex
            curPLayingSong = song
        }
    }

    private fun subscribeToObservers() {
        mainViewModel.mediaItems.observe(this) {
            it?.let { result->
                when(result.status) {
                    Status.SUCCESS -> {
                        result.data?.let { songs ->
                            swipeSongAdapter.songs = songs
                            if(songs.isNotEmpty()) {
                                glide.load((curPLayingSong ?: songs[0]).imageURL).into(ivCurSongImage)
                            }
                            switchViewPagerToCurrentSong(curPLayingSong ?: return@observe)
                        }
                    }
                    Status.ERROR -> Unit
                    Status.LOADING -> Unit
                }
            }
        }

        mainViewModel.curPlayingSong.observe(this) {
            if(it == null) return@observe

            curPLayingSong = it.toSong()
            glide.load(curPLayingSong?.imageURL).into(ivCurSongImage)
            switchViewPagerToCurrentSong(curPLayingSong ?: return@observe)
        }

    }

}