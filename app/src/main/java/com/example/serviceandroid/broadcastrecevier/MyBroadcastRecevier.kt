package com.example.serviceandroid.broadcastrecevier

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.serviceandroid.service.SongSerivceMedia
import com.example.serviceandroid.service.SongService
import com.example.serviceandroid.service.SongServiceNew

class MyBroadcastRecevier: BroadcastReceiver() {
    override fun onReceive(p0: Context?, intent: Intent?) {
        val action : Int = intent!!.getIntExtra("handle_action_music", 0)

        val intentService = Intent(p0, SongServiceNew::class.java)
        intentService.putExtra("action_music", action)
        p0!!.startService(intentService)
    }
}