package com.example.serviceandroid

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.media.Image
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.CalendarContract.Colors
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.serviceandroid.adapter.MusicAdapter
import com.example.serviceandroid.interfaceClass.MusicChangeInterface
import com.example.serviceandroid.model.Song
import com.example.serviceandroid.service.MyService
import com.example.serviceandroid.service.SongSerivceMedia
import com.example.serviceandroid.service.SongService
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.LinearProgressIndicator
import java.util.Random

class MainActivity : AppCompatActivity(), MusicChangeInterface {

    lateinit var layout_media: LinearLayout
    lateinit var imageView: ImageView
    lateinit var nameSong: TextView
    lateinit var nameSingle: TextView
    lateinit var btnPlayorPause: ImageButton
    lateinit var btnClose: CardView
    lateinit var btnCancelStop: ImageButton
    lateinit var btnSkipNext: ImageButton
    lateinit var btnSkipPrevious: ImageButton
    lateinit var recyclerView: RecyclerView
    lateinit var musicAdapter: MusicAdapter
    private lateinit var mSong : Song
    private lateinit var musicProgress: SeekBar
    var isPlaying: Boolean = false
    var durationTotal = 0
    var durationCurrent = 0
    lateinit var textDurationPosition: TextView
    lateinit var textDurationTotal: TextView

    lateinit var layoutRepeat: CardView
    lateinit var btnRepeat: ImageButton
    lateinit var layoutRandom: CardView
    lateinit var btnRandom: ImageButton


    private val broadcastReceiver = object :BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            val song = intent!!.getSerializableExtra("song")
            mSong = song as Song
            isPlaying = intent!!.getBooleanExtra("isPlaying", false)
            var action: Int = intent!!.getIntExtra("action_music", 0)
            handleActionMusic(action)
            if(intent!!.action == "music_change"){
                Toast.makeText(this@MainActivity, "12312", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val broadcastChangeMusic = object : BroadcastReceiver(){
        override fun onReceive(p0: Context?, p1: Intent?) {
            Untils.musicChange = Untils.musicCurrent
            musicAdapter.notifyDataSetChanged()
        }
    }

    private val broadcastDuration = object : BroadcastReceiver(){
        override fun onReceive(p0: Context?, intent: Intent?) {
            durationTotal = intent!!.getIntExtra("duration", 0)
            durationCurrent = intent!!.getIntExtra("currentDuration", 0)
            musicProgress.max = durationTotal
            musicProgress.progress = durationCurrent
            textDurationPosition.setText(formatTime(durationCurrent))
            textDurationTotal.setText(formatTime(durationTotal))

            if(formatTime(durationCurrent) == formatTime(durationTotal)){
                if(Untils.isReapeat == true && Untils.isRandom == false){
                    val intent = Intent(this@MainActivity, SongSerivceMedia::class.java)
                    intent.putExtra("action_music", SongSerivceMedia.ACTION_CHANGE_MUSIC)
                    startService(intent)
                }
                else if(Untils.isReapeat == false && Untils.isRandom == true){
                    randomMusic()
                    musicAdapter.notifyDataSetChanged()
                    val intent = Intent(this@MainActivity, SongSerivceMedia::class.java)
                    intent.putExtra("action_music", SongSerivceMedia.ACTION_CHANGE_MUSIC)
                    startService(intent)
                }
                else
                if(Untils.musicCurrent == Untils.musicCount -1){
                    Untils.musicChange = -1
                    Untils.musicCurrent = -1
                    musicAdapter.notifyDataSetChanged()
                    val intent = Intent(this@MainActivity, SongSerivceMedia::class.java)
                    intent.putExtra("action_music", SongService.ACTION_CLEAR)
                    startService(intent)
                }else{
                    Untils.musicCurrent++
                    Untils.musicChange++
                    musicAdapter.notifyDataSetChanged()
                    val intent = Intent(this@MainActivity, SongSerivceMedia::class.java)
                    intent.putExtra("action_music", SongSerivceMedia.ACTION_CHANGE_MUSIC)
                    startService(intent)
                }
            }
        }
    }

    private fun formatTime(milliseconds: Int): String {
        val minutes = (milliseconds / 1000) / 60
        val seconds = (milliseconds / 1000) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    private fun handleActionMusic(action: Int) {
        when(action){
            SongService.ACTION_START -> {
                layout_media.visibility = View.VISIBLE
                showInfoMusic()
            }
            SongService.ACTION_PLAY -> {
                layout_media.visibility = View.VISIBLE
                showInfoMusic()
            }
            SongService.ACTION_PAUSE -> {
                layout_media.visibility = View.VISIBLE
                showInfoMusic()
            }
            SongService.ACTION_CLEAR -> {
                layout_media.visibility = View.GONE
            }
        }
    }

    @SuppressLint("SuspiciousIndentation", "ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initUI()

        registerReceiver(broadcastReceiver, IntentFilter("sendToActivity"))
        registerReceiver(broadcastChangeMusic, IntentFilter("music_change"))
        registerReceiver(broadcastDuration, IntentFilter("duration"))

        btnPlayorPause.setOnClickListener {
            val intent = Intent(this, SongSerivceMedia::class.java)
            if(isPlaying == false){
                intent.putExtra("action_music", SongService.ACTION_PLAY)
                isPlaying = true
            }else{
                intent.putExtra("action_music", SongService.ACTION_PAUSE)
                isPlaying = false
            }
            setStatusPlayOrPause()
            startService(intent)
        }

        musicProgress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                textDurationPosition.setText(formatTime(p1))
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
                val intent = Intent("change_seekbar")
                intent.putExtra("change", -1)
                sendBroadcast(intent)
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                val intent = Intent("change_seekbar")
                intent.putExtra("change", p0!!.progress)
                sendBroadcast(intent)
            }

        })

        btnSkipNext.setOnClickListener {
            val intent = Intent(this, SongSerivceMedia::class.java)
                intent.putExtra("action_music", SongSerivceMedia.ACTION_SKIPNEXT)
            startService(intent)
        }

        btnSkipPrevious.setOnClickListener {
            val intent = Intent(this, SongSerivceMedia::class.java)
            intent.putExtra("action_music", SongSerivceMedia.ACTION_SKIPPREVIOUS)
            startService(intent)
        }

        btnCancelStop.setOnClickListener {
            Untils.musicChange = -1
            Untils.musicCurrent = -1
            musicAdapter.notifyDataSetChanged()
            val intent = Intent(this, SongSerivceMedia::class.java)
            intent.putExtra("action_music", SongService.ACTION_CLEAR)
            startService(intent)
        }

        btnRepeat.setOnClickListener {
            Untils.isReapeat = !Untils.isReapeat
            Untils.isRandom = false
            if(Untils.isReapeat == true){
                Toast.makeText(this, "Đã bật lặp lại bài hát", Toast.LENGTH_SHORT).show()
                layoutRepeat.setCardBackgroundColor(Color.parseColor("#FF018786"))
                layoutRandom.setCardBackgroundColor(Color.parseColor("#000000"))
            }else{
                Toast.makeText(this, "Đã tắt lặp lại bài hát", Toast.LENGTH_SHORT).show()
                layoutRepeat.setCardBackgroundColor(Color.parseColor("#000000"))
            }
        }

        btnRandom.setOnClickListener {
            Untils.isRandom = !Untils.isRandom
            Untils.isReapeat = false
            if(Untils.isRandom == true){
                Toast.makeText(this, "Đã bật lặp lại bài hát", Toast.LENGTH_SHORT).show()
                layoutRandom.setCardBackgroundColor(Color.parseColor("#FF018786"))
                layoutRepeat.setCardBackgroundColor(Color.parseColor("#000000"))
            }else{
                Toast.makeText(this, "Đã tắt lặp lại bài hát", Toast.LENGTH_SHORT).show()
                layoutRandom.setCardBackgroundColor(Color.parseColor("#000000"))
            }
        }
    }

    private fun randomMusic(){
        val random = kotlin.random.Random.nextInt(0, Untils.musicCount)
        Untils.musicChange = random
        Untils.musicCurrent = random
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(broadcastReceiver)
    }

    private fun initUI() {
        layout_media = findViewById(R.id.media_player_bar_view)
        nameSong = findViewById(R.id.nameSong)
        nameSingle = findViewById(R.id.nameSingle)
        btnPlayorPause = findViewById(R.id.btnPlayOrPause)
        btnClose = findViewById(R.id.btnClose)
        imageView = findViewById(R.id.imageView)
        btnCancelStop = findViewById(R.id.btnCancelStop)
        btnSkipNext = findViewById(R.id.btnSkipNext)
        btnSkipPrevious = findViewById(R.id.btnSkipPrevious)
        recyclerView = findViewById(R.id.recycleview)
        recyclerView.layoutManager = LinearLayoutManager(this)
        musicAdapter = MusicAdapter(this, Untils.list)
        recyclerView.adapter = musicAdapter
        musicProgress = findViewById(R.id.media_player_bar_progress)
        textDurationTotal = findViewById(R.id.textDurationTotal)
        textDurationPosition = findViewById(R.id.textDurationPosition)

        layoutRepeat = findViewById(R.id.reapeat)
        layoutRandom = findViewById(R.id.random)
        btnRepeat = findViewById(R.id.btnRepeat)
        btnRandom = findViewById(R.id.btnRandom)
    }

    private fun showInfoMusic(){
        if(mSong != null){
            imageView.setImageResource(mSong.image!!)
            nameSong.setText(mSong.title)
            nameSingle.setText(mSong.single)
        }
        setStatusPlayOrPause()
    }

    private fun setStatusPlayOrPause(){
        if(isPlaying == false){
            btnPlayorPause.setImageResource(R.drawable.play)
        }else{
            btnPlayorPause.setImageResource(R.drawable.pause)
        }
    }

    override fun onMusicChange(musicChange: Int) {
        Untils.musicChange = musicChange
        Toast.makeText(this@MainActivity, Untils.musicChange.toString(), Toast.LENGTH_SHORT).show()
        musicAdapter.getDataSaveChange(Untils.list)
    }
}