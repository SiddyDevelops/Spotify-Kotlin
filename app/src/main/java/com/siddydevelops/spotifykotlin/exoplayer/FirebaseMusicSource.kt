package com.siddydevelops.spotifykotlin.exoplayer

import com.siddydevelops.spotifykotlin.exoplayer.State.*

class FirebaseMusicSource {

    private val onReadyListeners = mutableListOf<(Boolean)->Unit>()

    private var state: State = STATE_CREATED
    set(value) {
        if(value == STATE_INITILIZED || value == State.STATE_ERROR) {
            synchronized(onReadyListeners) {
                field = value
                onReadyListeners.forEach{ listener->
                    listener(state == STATE_INITILIZED)
                }
            }
        } else {
            field = value
        }
    }

    fun whenReady(action: (Boolean)->Unit): Boolean {
        return if(state == STATE_CREATED || state == STATE_INITILIZING) {
            onReadyListeners += action
            false
        } else {
            action(state == STATE_INITILIZED)
            true
        }
    }

}

enum class State{
    STATE_CREATED,
    STATE_INITILIZING,
    STATE_INITILIZED,
    STATE_ERROR
}