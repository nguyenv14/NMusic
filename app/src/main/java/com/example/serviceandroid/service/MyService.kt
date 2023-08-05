package com.example.serviceandroid.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.serviceandroid.MainActivity
import com.example.serviceandroid.R
import java.util.Date

class MyService: Service() {
    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        Log.i("Nguyen", "Start service")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        var mess = intent!!.getStringExtra("message").toString()
        Toast.makeText(this, mess, Toast.LENGTH_SHORT).show()
        sendNotification(mess);
        return START_NOT_STICKY

    }

    private fun sendNotification(mess: String) {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        var notification : Notification = NotificationCompat.Builder(this, "channel_1")
            .setContentTitle("This is foreground")
            .setContentText(mess)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentIntent(pendingIntent)
            .build()
        startForeground(getNotificationId(), notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i("Nguyen", "Destroy Service1")
    }

    private fun getNotificationId(): Int {
        return Date().time.toInt()
    }


}