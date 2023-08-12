package com.example.serviceandroid.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Icon
import android.media.MediaPlayer
import android.media.MediaSession2Service.MediaNotification
import android.media.session.MediaSession
import android.os.Handler
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import android.widget.RemoteViews
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.net.toUri
import com.example.serviceandroid.MainActivity
import com.example.serviceandroid.R
import com.example.serviceandroid.Untils
import com.example.serviceandroid.adapter.MusicAdapter
import com.example.serviceandroid.broadcastrecevier.MyBroadcastRecevier
import com.example.serviceandroid.interfaceClass.MusicChangeInterface
import com.example.serviceandroid.model.Song
import com.example.serviceandroid.model.SongModel
import java.io.Serializable
import java.util.Date

class SongSerivceMedia: Service() {

    lateinit var mediaPlayer: MediaPlayer
    var isPlaying = false
    lateinit var mSong: SongModel
    private val handler = Handler()
    var isUpdatingSeekBar = false
    var intentDuration = Intent("duration")
    var broadcastRecevier: BroadcastReceiver = object : BroadcastReceiver(){
        override fun onReceive(p0: Context?, p1: Intent?) {
            val count = p1!!.getIntExtra("change", 0)
            if(count < 0){
                stopUpdatingSeekBar()
            }else{
                isUpdatingSeekBar = true
                updateDurationProgress()
                mediaPlayer.seekTo(count)
            }
        }
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        Log.i("Nguyen", "Start service")
        registerReceiver(broadcastRecevier, IntentFilter("change_seekbar"))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val song: Serializable? = intent!!.getSerializableExtra("song");
        if(song != null){
            mSong = song as SongModel
            startMusic(song);
            sendNotification(song);
        }
        val action_music: Int = intent.getIntExtra("action_music", 0);
        handleActionMusic(action_music)
        return Service.START_NOT_STICKY
    }

    private fun startMusic(song: SongModel) {
        mediaPlayer = MediaPlayer.create(applicationContext, song.mp3!!.toUri())
        mediaPlayer.start()
        isPlaying = true
        isUpdatingSeekBar = true
        updateDurationProgress()

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
            ACTION_SKIPNEXT -> {
                skipNext()
            }
            ACTION_SKIPPREVIOUS -> {
                skipPrevious()
            }
            ACTION_CHANGE_MUSIC -> {
                if(Untils.musicCurrent == -1){
                    Untils.musicCurrent = Untils.musicChange
                    mSong = Untils.listSong.get(Untils.musicCurrent)
                    startMusic(mSong)
                    sendNotification(mSong)
                }else{
                    mediaPlayer.stop()
                    Untils.musicCurrent = Untils.musicChange
                    mSong = Untils.listSong.get(Untils.musicCurrent)
                    startMusic(mSong)
                    sendNotification(mSong)
                }
            }
        }
    }

    private fun skipPrevious() {
        if(Untils.musicCurrent <= 0){
            Toast.makeText(this, "Không có bài hát nào", Toast.LENGTH_SHORT).show()
        }else{
            Untils.musicCurrent--;
            mediaPlayer.stop()
            mSong = Untils.listSong.get(Untils.musicCurrent)
            startMusic(mSong)
            sendNotification(mSong);
            val intent = Intent("music_change")
            sendBroadcast(intent)
        }
    }

    private fun skipNext() {
        if(Untils.musicCurrent >= Untils.musicCount - 1){
            Toast.makeText(this, "Không có bài hát nào", Toast.LENGTH_SHORT).show()
        }else{
            Untils.musicCurrent++;
            mediaPlayer.stop()
            mSong = Untils.listSong.get(Untils.musicCurrent)
            startMusic(mSong)
            sendNotification(mSong);
            val intent = Intent("music_change")
            sendBroadcast(intent)
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
        stopUpdatingSeekBar()
        stopSelf()
        sendToACtivity(ACTION_CLEAR)
    }

    @SuppressLint("SuspiciousIndentation")
    private fun sendNotification(song: SongModel) {
        val intent = Intent(this, MainActivity::class.java)

        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        val mediaSession = MediaSessionCompat(this, "tag")
        val bitmap = BitmapFactory.decodeResource(this.resources, R.drawable.music)
        var notificationBuild: NotificationCompat.Builder = NotificationCompat.Builder(this, "channel_1")
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSmallIcon(R.mipmap.music_check)
            .setLargeIcon(bitmap)
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle()
                .setShowActionsInCompactView(0, 1, 2 /* #1: pause button \*/)
                .setMediaSession(mediaSession.getSessionToken()))
            .setContentTitle(song.nameSong)
            .setContentText(song.nameSingle)
            if(isPlaying == true){
                if(Untils.musicCurrent == Untils.musicCount - 1){
                    notificationBuild.addAction(R.drawable.skip_previous, "Previous", getPendingIntent(this, ACTION_SKIPPREVIOUS)) // #0
                        .addAction(R.drawable.pause, "Pause", getPendingIntent(this, ACTION_PAUSE)) // #1
                        .addAction(R.drawable.cancel, "Cancel", getPendingIntent(this, ACTION_CLEAR))
                }else if(Untils.musicCurrent == 0){
                    notificationBuild
                        .addAction(R.drawable.pause, "Pause", getPendingIntent(this, ACTION_PAUSE)) // #1
                        .addAction(R.drawable.skip_next, "Next", getPendingIntent(this, ACTION_SKIPNEXT)) // #2
                        .addAction(R.drawable.cancel, "Cancel", getPendingIntent(this, ACTION_CLEAR))
                }else{
                    notificationBuild.addAction(R.drawable.skip_previous, "Previous", getPendingIntent(this, ACTION_SKIPPREVIOUS)) // #0
                        .addAction(R.drawable.pause, "Pause", getPendingIntent(this, ACTION_PAUSE)) // #1
                        .addAction(R.drawable.skip_next, "Next", getPendingIntent(this, ACTION_SKIPNEXT)) // #2
                        .addAction(R.drawable.cancel, "Cancel", getPendingIntent(this, ACTION_CLEAR))
                }
            }else{
                if(Untils.musicCurrent == Untils.musicCount - 1){
                    notificationBuild.addAction(R.drawable.skip_previous, "Previous", getPendingIntent(this, ACTION_SKIPPREVIOUS)) // #0
                        .addAction(R.drawable.play, "Pause", getPendingIntent(this, ACTION_PLAY)) // #1
                        .addAction(R.drawable.cancel, "Cancel", getPendingIntent(this, ACTION_CLEAR))
                }else if(Untils.musicCurrent == 0){
                    notificationBuild
                        .addAction(R.drawable.play, "Pause", getPendingIntent(this, ACTION_PLAY)) // #1
                        .addAction(R.drawable.skip_next, "Next", getPendingIntent(this, ACTION_SKIPNEXT)) // #2
                        .addAction(R.drawable.cancel, "Cancel", getPendingIntent(this, ACTION_CLEAR))
                }else{
                    notificationBuild.addAction(R.drawable.skip_previous, "Previous", getPendingIntent(this, ACTION_SKIPPREVIOUS)) // #0
                        .addAction(R.drawable.play, "Pause", getPendingIntent(this, ACTION_PLAY)) // #1
                        .addAction(R.drawable.skip_next, "Next", getPendingIntent(this, ACTION_SKIPNEXT)) // #2
                        .addAction(R.drawable.cancel, "Cancel", getPendingIntent(this, ACTION_CLEAR))
                }
            }
        val notification = notificationBuild.build()
        startForeground(getNotificationId(), notification)
    }

    private fun getPendingIntent(context: Context, action: Int) : PendingIntent {
        val intent : Intent = Intent(this, MyBroadcastRecevier::class.java)
        intent.putExtra("handle_action_music", action)
        return PendingIntent.getBroadcast(this, action, intent, PendingIntent.FLAG_MUTABLE)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i("Nguyen", "Destroy Service1")
        if(mediaPlayer != null){
            mediaPlayer.release()
//            mediaPlayer.
            mediaPlayer = MediaPlayer()
        }
    }

    private fun stopUpdatingSeekBar() {
        isUpdatingSeekBar = false
        handler.removeCallbacksAndMessages(null)
    }

    private fun updateDurationProgress() {
        if(isUpdatingSeekBar == false){
            return
        }
        val duration = mediaPlayer.duration
        val currentPosition = mediaPlayer.currentPosition

        intentDuration.putExtra("duration", duration)
        intentDuration.putExtra("currentDuration", currentPosition)
        sendBroadcast(intentDuration)
        handler.postDelayed({
            updateDurationProgress()
        }, 500) // Cập nhật lại mỗi giây (500 mili giây)
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
        var ACTION_SKIPNEXT = 5
        var ACTION_SKIPPREVIOUS = 6
        var ACTION_CHANGE_MUSIC = 7
        var ACTION_PROGRESS = 8
        var ACTION_PROGRESS_ACTIVITY = 9
        var ACTION_PROGRESS_UNACTIVITY = 10
    }
}