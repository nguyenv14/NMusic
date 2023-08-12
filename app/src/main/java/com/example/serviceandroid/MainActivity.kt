package com.example.serviceandroid

import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.graphics.Color
import android.media.Image
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.provider.CalendarContract.Colors
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.Button
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
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.example.serviceandroid.adapter.MusicAdapter
import com.example.serviceandroid.interfaceClass.MusicChangeInterface
import com.example.serviceandroid.model.Song
import com.example.serviceandroid.model.SongModel
import com.example.serviceandroid.presenter.Main.MainInterface
import com.example.serviceandroid.presenter.Main.MainPresenter
import com.example.serviceandroid.service.MyService
import com.example.serviceandroid.service.PlayerService
import com.example.serviceandroid.service.SongSerivceMedia
import com.example.serviceandroid.service.SongService
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.Random

class MainActivity : AppCompatActivity(), MusicChangeInterface, MainInterface {

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
    private lateinit var mSong : SongModel
    private lateinit var musicProgress: SeekBar
//    var isPlaying: Boolean = false
    var durationTotal = 0
    var durationCurrent = 0
    lateinit var textDurationPosition: TextView
    lateinit var textDurationTotal: TextView

    lateinit var layoutRepeat: CardView
    lateinit var btnRepeat: ImageButton
    lateinit var layoutRandom: CardView
    lateinit var btnRandom: ImageButton

    lateinit var btnChooseMp3: Button
    lateinit var metadataRetriever: MediaMetadataRetriever
    lateinit var listMusic: List<SongModel>

    lateinit var checkEmpty: ImageView
    lateinit var mainPresenter: MainPresenter


    private val broadcastReceiver = object :BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            val song = intent!!.getSerializableExtra("song")
            mSong = song as SongModel
//            isPlaying = intent!!.getBooleanExtra("isPlaying", false)
            var action: Int = intent!!.getIntExtra("action_music", 0)
            handleActionMusic(action)
            if(intent!!.action == "music_change"){
            }
        }
    }

    private val broadcastChangeMusic = object : BroadcastReceiver(){
        override fun onReceive(p0: Context?, p1: Intent?) {
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
                    val intent = Intent(this@MainActivity, SongService::class.java)
                    intent.putExtra("action_music", SongSerivceMedia.ACTION_CHANGE_MUSIC)
                    startService(intent)
                }
                else if(Untils.isReapeat == false && Untils.isRandom == true){
                    randomMusic()
                    musicAdapter.notifyDataSetChanged()
                    val intent = Intent(this@MainActivity, SongService::class.java)
                    intent.putExtra("action_music", SongSerivceMedia.ACTION_CHANGE_MUSIC)
                    startService(intent)
                }
                else if(Untils.musicCurrent == Untils.musicCount -1){
                    Untils.musicChange = -1
                    Untils.musicCurrent = -1
                    musicAdapter.notifyDataSetChanged()
                    val intent = Intent(this@MainActivity, SongService::class.java)
                    intent.putExtra("action_music", SongService.ACTION_CLEAR)
                    startService(intent)
                }else{
                    Untils.musicCurrent++
                    Untils.musicChange = Untils.musicCurrent
                    musicAdapter.notifyDataSetChanged()
                    val intent = Intent(this@MainActivity, SongService::class.java)
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
                musicAdapter.notifyDataSetChanged()
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
                musicAdapter.notifyDataSetChanged()
                layout_media.visibility = View.GONE
            }
        }
    }

    private val REQUEST_CODE_PICK_MP3 = 1

    @SuppressLint( "ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        metadataRetriever = MediaMetadataRetriever()
//        doBindService()
        mainPresenter = MainPresenter(this, this)
        initUI()
        registerReceiver(broadcastReceiver, IntentFilter("sendToActivity"))
        registerReceiver(broadcastChangeMusic, IntentFilter("music_change"))
        registerReceiver(broadcastDuration, IntentFilter("duration"))
        mainPresenter.getDataMusicList()
        btnPlayorPause.setOnClickListener {
            val intent = Intent(this, SongService::class.java)
            if(Untils.isPlaying == false){
                intent.putExtra("action_music", SongService.ACTION_PLAY)
                Untils.isPlaying = true
            }else{
                intent.putExtra("action_music", SongService.ACTION_PAUSE)
                Untils.isPlaying = false
            }
            setStatusPlayOrPause()
            startService(intent)
        }

        btnChooseMp3.setOnClickListener {
            val intent = Intent(this, CreateSongActivity::class.java)
            startActivity(intent)
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
            val intent = Intent(this, SongService::class.java)
            intent.putExtra("action_music", SongSerivceMedia.ACTION_SKIPNEXT)
            startService(intent)
        }

        btnSkipPrevious.setOnClickListener {
            val intent = Intent(this, SongService::class.java)
            intent.putExtra("action_music", SongSerivceMedia.ACTION_SKIPPREVIOUS)
            startService(intent)
        }

        btnCancelStop.setOnClickListener {
            Untils.musicChange = -1
            Untils.musicCurrent = -1
            musicAdapter.notifyDataSetChanged()
            val intent = Intent(this, SongService::class.java)
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
//        player = ExoPlayer.Builder(this@MainActivity).build()
    }

//    private fun doBindService() {
//        val intentService = Intent(this, PlayerService::class.java)
//        bindService(intentService, playerService, Context.BIND_AUTO_CREATE)
//    }

//    val playerService: ServiceConnection = object : ServiceConnection {
//        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
//            val binder: PlayerService.ServiceBinder = p1 as PlayerService.ServiceBinder
//            mediaPlayerService = binder.getPlayerService()
//            isBound = true
//        }
//
//        override fun onServiceDisconnected(p0: ComponentName?) {
//            isBound = false
//        }
//    }

    private fun randomMusic(){
        val random = kotlin.random.Random.nextInt(0, Untils.musicCount)
        Untils.musicChange = random
        Untils.musicCurrent = random
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(broadcastReceiver)
//        doUnBind()
    }

//    private fun doUnBind() {
//        if(isBound){
//            unbindService(playerService)
//            isBound = false
//        }
//    }

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
        musicProgress = findViewById(R.id.media_player_bar_progress)
        textDurationTotal = findViewById(R.id.textDurationTotal)
        textDurationPosition = findViewById(R.id.textDurationPosition)

        layoutRepeat = findViewById(R.id.reapeat)
        layoutRandom = findViewById(R.id.random)
        btnRepeat = findViewById(R.id.btnRepeat)
        btnRandom = findViewById(R.id.btnRandom)

        btnChooseMp3 = findViewById(R.id.btnChooseMp3)
        checkEmpty = findViewById(R.id.checkEmpty)
    }

    private fun showInfoMusic(){
        if(mSong != null){
//            imageView.setImageResource(mSong.image!!)
//            nameSong.setText(mSong.title)
//            nameSingle.setText(mSong.single)
            Glide.with(this).load(mSong.image).into(imageView)
            nameSingle.setText(mSong.nameSingle)
            nameSong.setText(mSong.nameSong)
        }
        setStatusPlayOrPause()
    }

    private fun setStatusPlayOrPause(){
        if(Untils.isPlaying == false){
            btnPlayorPause.setImageResource(R.drawable.play)
        }else{
            btnPlayorPause.setImageResource(R.drawable.pause)
        }
    }

//    override fun onMusicChange(musicChange: Int) {
//        Untils.musicChange = musicChange
//        Toast.makeText(this@MainActivity, Untils.musicChange.toString(), Toast.LENGTH_SHORT).show()
//        musicAdapter.getDataSaveChange(Untils.list)
//    }

    override fun getListSongSuccess(listSong: List<SongModel>) {
        listMusic = listSong
        Untils.musicCount = listMusic.size
        musicAdapter = MusicAdapter(this@MainActivity, listMusic)
        recyclerView.adapter = musicAdapter
        recyclerView.visibility = View.VISIBLE
        checkEmpty.visibility = View.GONE
    }

    override fun getListSongError() {
        TODO("Not yet implemented")
        recyclerView.visibility = View.GONE
        checkEmpty.visibility = View.VISIBLE
    }

    override fun onMusicChange(musicChange: Int) {
        TODO("Not yet implemented")
    }
}