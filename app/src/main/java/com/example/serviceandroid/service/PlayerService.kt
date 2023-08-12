package com.example.serviceandroid.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.media.AudioAttributes
import android.os.Binder
import android.os.IBinder
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.serviceandroid.MainActivity
import com.example.serviceandroid.R
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.util.NotificationUtil.IMPORTANCE_HIGH

class PlayerService: Service() {
    lateinit var player: ExoPlayer
    lateinit var notificationManager: PlayerNotificationManager
    var serviceBinder = ServiceBinder()
    override fun onBind(p0: Intent?): IBinder? {
        return serviceBinder
    }

    class ServiceBinder: Binder(){
        fun getPlayerService() : PlayerService{
            return PlayerService()
        }
    }

    override fun onCreate() {
        super.onCreate()
        Toast.makeText(this, "211", Toast.LENGTH_SHORT).show()
        Log.i("Nguyên", "123131")
        player = ExoPlayer.Builder(applicationContext).build()
        Log.i("Nguyên", "55551")
        val audioAttributes = com.google.android.exoplayer2.audio.AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.CONTENT_TYPE_MUSIC).build()
        player.setAudioAttributes(audioAttributes, true)

        val notificationId = 1111
        notificationManager = PlayerNotificationManager.Builder(this, notificationId, "Music_channel")
            .setNotificationListener(notifiListener)
            .setMediaDescriptionAdapter(descriptionAdapter)
            .setChannelImportance(IMPORTANCE_HIGH)
            .setSmallIconResourceId(R.drawable.music)
            .setChannelDescriptionResourceId(R.string.app_name)
            .setNextActionIconResourceId(R.drawable.skip_next)
            .setPauseActionIconResourceId(R.drawable.pause)
            .setPlayActionIconResourceId(R.drawable.play)
            .setPreviousActionIconResourceId(R.drawable.skip_previous)
            .setChannelNameResourceId(R.string.app_name)
            .build()

        notificationManager.setPlayer(player)
        notificationManager.setPriority(NotificationCompat.PRIORITY_MAX)
        notificationManager.setUseRewindAction(false)
        notificationManager.setUseFastForwardAction(false)
    }

    var notifiListener: PlayerNotificationManager.NotificationListener = object :
        PlayerNotificationManager.NotificationListener {
        override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
            super.onNotificationCancelled(notificationId, dismissedByUser)
            stopForeground(notificationId)
            if(player.isPlaying){
                player.pause()
            }
        }

        override fun onNotificationPosted(
            notificationId: Int,
            notification: Notification,
            ongoing: Boolean
        ) {
            Toast.makeText(this@PlayerService, "222", Toast.LENGTH_SHORT).show()
            startForeground(notificationId, notification)
        }
    }

    val descriptionAdapter = object : PlayerNotificationManager.MediaDescriptionAdapter {
        override fun getCurrentContentTitle(player: Player): CharSequence {
            return player.currentMediaItem!!.mediaMetadata.title!!;
        }

        override fun createCurrentContentIntent(player: Player): PendingIntent? {
            TODO("Not yet implemented")
            val intent = Intent(applicationContext, MainActivity::class.java)
            return PendingIntent.getActivity(applicationContext, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        }

        override fun getCurrentContentText(player: Player): CharSequence? {
            TODO("Not yet implemented")
        }

        override fun getCurrentLargeIcon(
            player: Player,
            callback: PlayerNotificationManager.BitmapCallback
        ): Bitmap? {
            val view = ImageView(applicationContext)
            view.setImageURI(player.currentMediaItem!!.mediaMetadata.artworkUri)

            var bitmap: BitmapDrawable = view.drawable as BitmapDrawable
            if(bitmap == null){
                bitmap = ContextCompat.getDrawable(applicationContext, R.drawable.music) as BitmapDrawable
            }

            assert(bitmap != null)
            return bitmap.bitmap
        }

    }

    override fun onDestroy() {
        if(player.isPlaying){
            player.stop()
        }
        notificationManager.setPlayer(null)
        player.release()
        stopForeground(true)
        stopSelf()
        super.onDestroy()

    }

}