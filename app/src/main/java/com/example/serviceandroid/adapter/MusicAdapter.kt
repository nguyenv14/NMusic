package com.example.serviceandroid.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.view.menu.MenuView.ItemView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.utils.Utils
import com.bumptech.glide.Glide
import com.example.serviceandroid.R
import com.example.serviceandroid.Untils
import com.example.serviceandroid.model.Song
import com.example.serviceandroid.model.SongModel
import com.example.serviceandroid.service.PlayerService
import com.example.serviceandroid.service.SongSerivceMedia
import com.example.serviceandroid.service.SongService
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.MediaMetadata
import com.google.android.exoplayer2.Player

class MusicAdapter(val context: Context, var listMusic: List<SongModel>): RecyclerView.Adapter<MusicAdapter.MusicViewHolder>() {

    class MusicViewHolder(itemview: View) : RecyclerView.ViewHolder(itemview){
        val imageview: ImageView = itemview.findViewById(R.id.imageView)
        val nameSong: TextView = itemview.findViewById(R.id.nameSong)
        val nameSingle: TextView = itemview.findViewById(R.id.nameSingle)
        val isPlaying: LottieAnimationView = itemview.findViewById(R.id.isPlaying)
        val layout_media: ConstraintLayout = itemview.findViewById(R.id.layout_media_music)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicViewHolder {
        val inflate = LayoutInflater.from(parent.context).inflate(R.layout.item_music_media, parent, false)
        return MusicViewHolder(inflate)
    }

    override fun getItemCount(): Int {
        if(listMusic.isEmpty()){
            return 0
        }
        return listMusic.size
    }

//    internal fun getDataSaveChange(list: List<Song>){
//        Untils.musicChange = Untils.musicCurrent
//        listMusic = list
//        notifyDataSetChanged()
//    }

    override fun onBindViewHolder(holder: MusicViewHolder, position: Int) {
        val dataCurrent: SongModel = listMusic.get(position)
        Glide.with(context).load(dataCurrent.image).into(holder.imageview)
        holder.nameSong.setText(dataCurrent.nameSong)
        holder.nameSingle.setText(dataCurrent.nameSingle)
        if(Untils.musicChange == position){
            holder.isPlaying.visibility = View.VISIBLE
        }else{
            holder.isPlaying.visibility = View.GONE
        }
        holder.layout_media.setOnClickListener {
//            context.startService(Intent(context, PlayerService::class.java))
//
//            if(!player.isPlaying){
//                player.setMediaItems(getMediaImtes(), position, 0)
//            }else{
//                player.pause()
//                player.seekTo(position, 0)
//            }
//            player.prepare()
//            player.play()
            if(Untils.musicCurrent == -1){
                Toast.makeText(context, position.toString(), Toast.LENGTH_SHORT).show()
                val song = Untils.listSong.get(position)
                Untils.musicCurrent = position
                Untils.musicChange = position
                val intent1 = Intent(context, SongService::class.java)
                intent1.putExtra("song", song)
//                intent1.putExtra("action_music", SongSerivceMedia.ACTION_CLEAR)
                context.startService(intent1)
            }else{
                Toast.makeText(context, position.toString() + "  123", Toast.LENGTH_SHORT).show()

                Untils.musicChange = position
                val intent = Intent(context, SongService::class.java)
                intent.putExtra("action_music", SongService.ACTION_CHANGE_MUSIC)
                context.startService(intent)
                notifyDataSetChanged()
            }
        }
    }

    private fun getMediaImtes(): List<MediaItem> {
        val list = ArrayList<MediaItem>()
        for(item in listMusic){
            val mediaItem = MediaItem.Builder().setUri(item.mp3).setMediaMetadata(getMetadata(item)).build()
            list.add(mediaItem)
        }
        return list
    }

    private fun getMetadata(item: SongModel): MediaMetadata {
        return MediaMetadata.Builder().setTitle(item.nameSong).setArtworkUri(item.image.toUri()).build()
    }


}