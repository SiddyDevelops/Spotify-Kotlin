package com.siddydevelops.spotifykotlin.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.RequestManager
import com.google.android.material.snackbar.Snackbar
import com.siddydevelops.spotifykotlin.R
import com.siddydevelops.spotifykotlin.adapters.SwipeSongAdapter
import com.siddydevelops.spotifykotlin.data.entities.Song
import com.siddydevelops.spotifykotlin.exoplayer.isPlaying
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

    private var playBackState: PlaybackStateCompat? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        window.navigationBarColor = resources.getColor(R.color.bluish_black)
        ivCurSongImage.clipToOutline = true
        vpSong.adapter = swipeSongAdapter
        subscribeToObservers()

        vpSong.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if(playBackState?.isPlaying == true) {
                    mainViewModel.playOrToggleSong(swipeSongAdapter.songs[position])
                } else {
                    curPLayingSong = swipeSongAdapter.songs[position]
                }
            }
        })

        ivPlayPause.setOnClickListener {
            curPLayingSong?.let {
                mainViewModel.playOrToggleSong(it,true)
            }
        }

        swipeSongAdapter.setItemClickListener {
            navHostFragment.findNavController().navigate(
                R.id.globalActionToSongFragment
            )
        }

        navHostFragment.findNavController().addOnDestinationChangedListener { _, destination, _ ->
            when(destination.id) {
                R.id.songFragment -> {hideBottomBar()
                    title_textView.text = "Currently Playing"}
                R.id.homeFragment -> showBottomBar()
                else -> showBottomBar()
            }
        }

    }

    private fun hideBottomBar() {
        ivCurSongImage.isVisible = false
        vpSong.isVisible = false
        ivPlayPause.isVisible = false
    }

    private fun showBottomBar() {
        ivCurSongImage.isVisible = true
        vpSong.isVisible = true
        ivPlayPause.isVisible = true
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

        mainViewModel.playbackState.observe(this) {
            playBackState = it
            ivPlayPause.setImageResource(
                if(playBackState?.isPlaying == true) R.drawable.ic_pause else R.drawable.ic_play
            )
        }

        mainViewModel.isConnected.observe(this) {
            it?.getContentIfNotHandled()?.let { result ->
                when(result.status) {
                    Status.ERROR -> Snackbar.make(rootLayout,
                        result.message ?: "An unknown error occurred!",
                    Snackbar.LENGTH_LONG).show()
                    else -> Unit
                }
            }
        }

        mainViewModel.networkError.observe(this) {
            it?.getContentIfNotHandled()?.let { result ->
                when(result.status) {
                    Status.ERROR -> Snackbar.make(rootLayout,
                        result.message ?: "An unknown error occurred!",
                        Snackbar.LENGTH_LONG).show()
                    else -> Unit
                }
            }
        }

    }

}