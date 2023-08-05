package com.example.serviceandroid

import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

class MyApplication : Application() {


    override fun onCreate() {
        super.onCreate()
        createChannelNotification();
    }

    private fun createChannelNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel.
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val mChannel = NotificationChannel(CHANNEL_ID, name, importance)
            mChannel.description = descriptionText
            mChannel.setSound(null, null)
            val notificationManager = getSystemService(NotificationManager::class.java) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)
        }
    }

    companion object {
        val CHANNEL_ID: String = "channel_1"
    }
}