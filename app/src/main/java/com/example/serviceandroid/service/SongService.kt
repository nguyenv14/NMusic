package com.example.serviceandroid.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import android.util.Log
import android.widget.RemoteViews
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.serviceandroid.MainActivity
import com.example.serviceandroid.R
import com.example.serviceandroid.broadcastrecevier.MyBroadcastRecevier
import com.example.serviceandroid.model.Song
import java.io.Serializable
import java.util.Date

class SongService: Service() {

    lateinit var mediaPlayer: MediaPlayer

    var isPlaying = false
    lateinit var mSong: Song

    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onCreate() {
        super.onCreate()
        Log.i("Nguyen", "Start service")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val song: Serializable? = intent!!.getSerializableExtra("song");
        if(song != null){
                mSong = song as Song
                startMusic(song);
                sendNotification(song);
        }
        val action_music: Int = intent.getIntExtra("action_music", 0);
        handleActionMusic(action_music)
        return START_NOT_STICKY
    }

    private fun startMusic(song: Song) {
        mediaPlayer = MediaPlayer.create(applicationContext, song.resource!!)
        mediaPlayer.start()
        isPlaying = true
        sendToACtivity(ACTION_START)
    }

    private fun  handleActionMusic(action : Int){
        when(action){
            ACTION_PLAY -> {
                playMusic()
                sendNotification(mSong)
            }
            ACTION_PAUSE ->{
                pauseMusic()
                sendNotification(mSong)
            }
            ACTION_CLEAR ->{
                clearMusic()
            }
        }
    }

    private fun  pauseMusic(){
        if(mediaPlayer != null && isPlaying == true){
            mediaPlayer.pause()
            isPlaying = false
        }
        sendToACtivity(ACTION_PAUSE)

    }

    private fun playMusic(){
        if(isPlaying == false && mediaPlayer != null){
            mediaPlayer.start()
            isPlaying = true
        }
        sendToACtivity(ACTION_PLAY)
    }

    private fun clearMusic(){
        stopSelf()
        sendToACtivity(ACTION_CLEAR)
    }

    private fun sendNotification(song: Song) {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val removoteView = RemoteViews(packageName, R.layout.layout_custom_notification_song)
        removoteView.setTextViewText(R.id.nameSong, song.title)
        removoteView.setTextViewText(R.id.nameSingle, song.single)
        removoteView.setImageViewResource(R.id.image, R.drawable.music)
        removoteView.setImageViewResource(R.id.btnPlayOrPause, R.drawable.pause_button)

        if(isPlaying == true){
            removoteView.setOnClickPendingIntent(R.id.btnPlayOrPause, getPendingIntent(this, ACTION_PAUSE))
            removoteView.setImageViewResource(R.id.btnPlayOrPause, R.drawable.pause_button)
        }else{
            removoteView.setOnClickPendingIntent(R.id.btnPlayOrPause, getPendingIntent(this, ACTION_PLAY))
            removoteView.setImageViewResource(R.id.btnPlayOrPause, R.drawable.play_button)
        }

        removoteView.setOnClickPendingIntent(R.id.btnClose, getPendingIntent(this, ACTION_CLEAR))


        var notification : Notification = NotificationCompat.Builder(this, "channel_1")
            .setCustomContentView(removoteView)
            .setSmallIcon(R.drawable.music)
            .setContentIntent(pendingIntent)
            .setSound(null)
            .build()
        startForeground(getNotificationId(), notification)
    }

    private fun getPendingIntent(context: Context, action: Int) : PendingIntent{
        val intent : Intent = Intent(this, MyBroadcastRecevier::class.java)
        intent.putExtra("handle_action_music", action)
        return PendingIntent.getBroadcast(this, action, intent, PendingIntent.FLAG_MUTABLE)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i("Nguyen", "Destroy Service1")
        if(mediaPlayer != null){
            mediaPlayer.release()
            mediaPlayer = MediaPlayer()
        }
    }

    private fun getNotificationId(): Int {
        return Date().time.toInt()
    }

    private fun sendToACtivity(action: Int){
        val intent = Intent("sendToActivity");
        intent.putExtra("song", mSong)
        intent.putExtra("isPlaying", isPlaying)
        intent.putExtra("action_music", action)

        sendBroadcast(intent)
    }

    companion object {
        var ACTION_PAUSE = 1
        var ACTION_PLAY = 2
        var ACTION_CLEAR = 3
        var ACTION_START = 4
    }
}