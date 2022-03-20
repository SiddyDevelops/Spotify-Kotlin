package com.siddydevelops.spotifykotlin.exoplayer

import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.siddydevelops.spotifykotlin.other.Constants.NETWORK_ERROR
import com.siddydevelops.spotifykotlin.other.Event
import com.siddydevelops.spotifykotlin.other.Resource

class MusicServiceConnection(context: Context) {

    private val _isConnected = MutableLiveData<Event<Resource<Boolean>>>()
    val isConnected: LiveData<Event<Resource<Boolean>>> = _isConnected

    private val _networkError = MutableLiveData<Event<Resource<Boolean>>>()
    val networkError: LiveData<Event<Resource<Boolean>>> = _networkError

    private val _playbackState = MutableLiveData<PlaybackStateCompat?>()
    val playbackState: LiveData<PlaybackStateCompat?> = _playbackState

    private val _currPlayingSong = MutableLiveData<MediaMetadataCompat?>()
    val currPlayingSong: LiveData<MediaMetadataCompat?> = _currPlayingSong

    lateinit var mediaController: MediaControllerCompat

    private val mediaBrowserConnectionCallback = MediaBrowserConnectCallback(context)

    private val mediaBrowser = MediaBrowserCompat(
        context,
        ComponentName(
            context,
            MusicService::class.java
        ),
        mediaBrowserConnectionCallback,
        null
    ).apply {
        connect()
    }

    val transportControls: MediaControllerCompat.TransportControls
            get() = mediaController.transportControls

    fun subscribe(parentId: String, callback: MediaBrowserCompat.SubscriptionCallback) {
        mediaBrowser.subscribe(parentId, callback)
    }

    fun unSubscribe(parentId: String, callback: MediaBrowserCompat.SubscriptionCallback) {
        mediaBrowser.unsubscribe(parentId, callback)
    }


    private inner class MediaBrowserConnectCallback(private val context: Context)
        : MediaBrowserCompat.ConnectionCallback() {
            override fun onConnected() {
                mediaController = MediaControllerCompat(context, mediaBrowser.sessionToken).apply {
                    registerCallback(MediaControllerCallback())
                }
                _isConnected.postValue(Event(Resource.success(true)))
            }

            override fun onConnectionSuspended() {
                _isConnected.postValue(Event(Resource.error(
                    "The connection was suspended.",
                    false
                )))
            }

            override fun onConnectionFailed() {
                _isConnected.postValue(Event(Resource.error(
                    "Couldn't connect to media browser.",
                    false
                )))
            }
        }

    private inner class MediaControllerCallback: MediaControllerCompat.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            _playbackState.postValue(state)
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            _currPlayingSong.postValue(metadata)
        }

        override fun onSessionEvent(event: String?, extras: Bundle?) {
            super.onSessionEvent(event, extras)
            when(event) {
                NETWORK_ERROR -> _networkError.postValue(Event(Resource.error(
                    "Couldn't connect to the server. Please check your internet connection.",
                    null
                )))
            }
        }

        override fun onSessionDestroyed() {
            mediaBrowserConnectionCallback.onConnectionSuspended()
        }
    }

}