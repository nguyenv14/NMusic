package com.example.serviceandroid

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.serviceandroid.adapter.MusicAdapter
import com.example.serviceandroid.interfaceClass.MusicChangeInterface
import com.example.serviceandroid.model.SongModel
import com.example.serviceandroid.presenter.Main.MainInterface
import com.example.serviceandroid.presenter.Main.MainPresenter
import com.example.serviceandroid.service.SongSerivceMedia
import com.example.serviceandroid.service.SongService
import com.example.serviceandroid.service.SongServiceNew
import com.google.android.exoplayer2.ExoPlayer


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
    lateinit var listMusic: List<SongModel>

    lateinit var checkEmpty: ImageView
    lateinit var mainPresenter: MainPresenter
    lateinit var player: ExoPlayer
    lateinit var mediaPlayer: MediaPlayer

    lateinit var media_player_bar_view: LinearLayout

    //layout Player
    lateinit var layoutPlayer: LinearLayout
    lateinit var btnBack: ImageView
    var showPlayer = false
    lateinit var  imageSong: ImageView
    lateinit var nameSongView: TextView
    lateinit var nameSinger: TextView



    private val broadcastReceiver = object :BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            val song = intent!!.getSerializableExtra("song")
            mSong = song as SongModel
            val action: Int = intent.getIntExtra("action_music", 0)
            handleActionMusic(action)
        }
    }

    private val broadcastChangeMusic = object : BroadcastReceiver(){
        override fun onReceive(p0: Context?, p1: Intent?) {
            musicAdapter.notifyDataSetChanged()
        }
    }

    private val broadcastDuration = object : BroadcastReceiver(){
        override fun onReceive(p0: Context?, intent: Intent?) {
            durationTotal = intent!!.getIntExtra("duration", 0).toInt()
            durationCurrent = intent.getIntExtra("currentDuration", 0).toInt()
            musicProgress.max = durationTotal.toInt()
            musicProgress.progress = durationCurrent.toInt()
            textDurationPosition.setText(formatTime(durationCurrent))
            textDurationTotal.setText(formatTime(durationTotal))

            if(formatTime(durationCurrent) == formatTime(durationTotal) && durationCurrent != 0){
                if(Untils.isReapeat){
                    val intent1 = Intent(this@MainActivity, SongServiceNew::class.java)
                    intent1.putExtra("action_music", SongServiceNew.ACTION_REPEAT)
                    startService(intent1)
                }
                else if(Untils.isRandom){
                    randomMusic()
                    val intent2 = Intent(this@MainActivity, SongServiceNew::class.java)
                    intent2.putExtra("action_music", SongServiceNew.ACTION_RANDOM)
                    startService(intent2)
                }
                else{
                    Untils.musicCurrent++
                    val intent3 = Intent(this@MainActivity, SongServiceNew::class.java)
                    intent3.putExtra("action_music", SongServiceNew.ACTION_START)
                    startService(intent3)
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
                showPlayerMusic()
            }
            SongService.ACTION_PLAY -> {
                layout_media.visibility = View.VISIBLE
                showInfoMusic()
                showPlayerMusic()
            }
            SongService.ACTION_PAUSE -> {
                layout_media.visibility = View.VISIBLE
                showInfoMusic()
                showPlayerMusic()
            }
            SongService.ACTION_CLEAR -> {
                musicAdapter.notifyDataSetChanged()
                layout_media.visibility = View.GONE
            }
            SongServiceNew.ACTION_SKIPNEXT -> {
                musicAdapter.notifyDataSetChanged()
                showInfoMusic()
                showPlayerMusic()
            }
            SongServiceNew.ACTION_SKIPPREVIOUS -> {
                musicAdapter.notifyDataSetChanged()
                showInfoMusic()
                showPlayerMusic()
            }
            SongServiceNew.ACTION_CHANGE_MUSIC ->{
                musicAdapter.notifyDataSetChanged()
                showInfoMusic()
                showPlayerMusic()
            }
        }
    }

    @SuppressLint( "ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mainPresenter = MainPresenter(this, this)
        initUI()
        registerReceiver(broadcastReceiver, IntentFilter("sendToActivity"))
        registerReceiver(broadcastChangeMusic, IntentFilter("music_change"))
        registerReceiver(broadcastDuration, IntentFilter("duration"))
        mainPresenter.getDataMusicList()

        btnChooseMp3.setOnClickListener {
            val intent = Intent(this, CreateSongActivity::class.java)
            startActivity(intent)
        }

        btnPlayorPause.setOnClickListener {
            val intent = Intent(this, SongServiceNew::class.java)
            if(Untils.isPlaying == false){
                intent.putExtra("action_music", SongServiceNew.ACTION_PLAY)
                Untils.isPlaying = true
            }else{
                intent.putExtra("action_music", SongServiceNew.ACTION_PAUSE)
                Untils.isPlaying = false
            }
            setStatusPlayOrPause()
            startService(intent)
        }

        mediaPlayer = MediaPlayer.create(this, R.raw.aloi)


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
            val intent = Intent(this, SongServiceNew::class.java)
            intent.putExtra("action_music", SongSerivceMedia.ACTION_SKIPNEXT)
            startService(intent)
        }
        btnSkipPrevious.setOnClickListener {
            val intent = Intent(this, SongServiceNew::class.java)
            intent.putExtra("action_music", SongSerivceMedia.ACTION_SKIPPREVIOUS)
            startService(intent)
        }

        btnCancelStop.setOnClickListener {
            Untils.musicChange = -1
            Untils.musicCurrent = -1
            musicAdapter.notifyDataSetChanged()
            val intent = Intent(this, SongServiceNew::class.java)
            intent.putExtra("action_music", SongService.ACTION_CLEAR)
            startService(intent)
        }

        btnRepeat.setOnClickListener {
            Untils.isReapeat = !Untils.isReapeat
            Untils.isRandom = false
            if(Untils.isReapeat == true){
                Toast.makeText(this, "Đã bật lặp lại bài hát", Toast.LENGTH_SHORT).show()
                layoutRepeat.setCardBackgroundColor(Color.parseColor("#000000"))
                layoutRandom.setCardBackgroundColor(Color.parseColor("#FFFFFF"))
                btnRandom.setImageResource(R.drawable.random)
                btnRepeat.setImageResource(R.drawable.repeat_white)
            }else{
                Toast.makeText(this, "Đã tắt lặp lại bài hát", Toast.LENGTH_SHORT).show()
                layoutRepeat.setCardBackgroundColor(Color.parseColor("#FFFFFF"))
                btnRepeat.setImageResource(R.drawable.repeat)
            }
        }
        btnRandom.setOnClickListener {
            Untils.isRandom = !Untils.isRandom
            Untils.isReapeat = false
            if(Untils.isRandom == true){
                Toast.makeText(this, "Đã bật ngẫu nhiên bài hát", Toast.LENGTH_SHORT).show()
                layoutRandom.setCardBackgroundColor(Color.parseColor("#000000"))
                btnRandom.setImageResource(R.drawable.random_white)
                layoutRepeat.setCardBackgroundColor(Color.parseColor("#FFFFFF"))
                btnRepeat.setImageResource(R.drawable.repeat)
            }else{
                Toast.makeText(this, "Đã tắt ngẫu nhiên bài hát", Toast.LENGTH_SHORT).show()
                layoutRandom.setCardBackgroundColor(Color.parseColor("#000000"))
                btnRandom.setImageResource(R.drawable.random)
            }
        }
        player = ExoPlayer.Builder(this@MainActivity).build()

        layoutPlayer.setOnClickListener {
        }

        btnBack.setOnClickListener {
            showPlayer = false
            showPlayer()
        }

        media_player_bar_view.setOnClickListener {
            showPlayer = true
            showPlayer()
        }

        startRotationAnimation()
    }

    private fun startRotationAnimation() {
        val rotateAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate_360)
        rotateAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
            }

            override fun onAnimationEnd(animation: Animation?) {
                startRotationAnimation()
            }
            override fun onAnimationRepeat(animation: Animation?) {
                // Animation đã lặp lại, bắt đầu Animation mới
                startRotationAnimation()
            }
        })
        imageSong.startAnimation(rotateAnimation)
    }

    private  fun showPlayer(){
        if(showPlayer){
            layoutPlayer.visibility = View.VISIBLE
        }else{
            layoutPlayer.visibility = View.GONE
        }
    }

    private fun randomMusic(){
        val random = kotlin.random.Random.nextInt(0, Untils.listSong.size)
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
        musicProgress = findViewById(R.id.media_player_bar_progress)
        textDurationTotal = findViewById(R.id.textDurationTotal)
        textDurationPosition = findViewById(R.id.textDurationPosition)
        layoutRepeat = findViewById(R.id.reapeat)
        layoutRandom = findViewById(R.id.random)
        btnRepeat = findViewById(R.id.btnRepeat)
        btnRandom = findViewById(R.id.btnRandom)
        btnChooseMp3 = findViewById(R.id.btnChooseMp3)
        checkEmpty = findViewById(R.id.checkEmpty)


        media_player_bar_view = findViewById(R.id.media_player_bar_view)
        btnBack = findViewById(R.id.btnBack)
        layoutPlayer = findViewById(R.id.layout_player)
        imageSong = findViewById(R.id.imageSong)
        nameSongView = findViewById(R.id.nameSongView)
        nameSinger = findViewById(R.id.nameSinger)
    }


    fun showPlayerMusic(){
        Glide.with(this).load(mSong.image).into(imageSong)
        nameSongView.setText(mSong.nameSong)
        nameSinger.setText(mSong.nameSingle)
    }
    private fun showInfoMusic(){
        if(mSong != null){
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





    override fun getListSongSuccess(listSong: List<SongModel>) {
        listMusic = listSong
        Untils.musicCount = listMusic.size
        musicAdapter = MusicAdapter(this@MainActivity, listMusic, player)
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