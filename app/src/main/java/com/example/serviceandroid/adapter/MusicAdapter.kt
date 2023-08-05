package com.example.serviceandroid.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.view.menu.MenuView.ItemView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.example.serviceandroid.R
import com.example.serviceandroid.Untils
import com.example.serviceandroid.model.Song
import com.example.serviceandroid.service.SongSerivceMedia

class MusicAdapter(val context: Context, var listMusic: List<Song>): RecyclerView.Adapter<MusicAdapter.MusicViewHolder>() {

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

    internal fun getDataSaveChange(list: List<Song>){
        Untils.musicChange = Untils.musicCurrent
        listMusic = list
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: MusicViewHolder, position: Int) {
        val dataCurrent: Song = listMusic.get(position)
        holder.imageview.setImageResource(dataCurrent.image!!)
        holder.nameSong.setText(dataCurrent.title)
        holder.nameSingle.setText(dataCurrent.single)
        if(Untils.musicChange == position){
            holder.isPlaying.visibility = View.VISIBLE
        }else{
            holder.isPlaying.visibility = View.GONE
        }
        holder.layout_media.setOnClickListener {
//            if(Untils.musicCurrent != -1){
//                val intent1 = Intent(context, SongSerivceMedia::class.java)
//                intent1.putExtra("action_music", SongSerivceMedia.ACTION_CLEAR)
//                context.startService(intent1)
//            }
//            val song: Song = Untils.list.get(position)
            Untils.musicChange = position
            val intent = Intent(context, SongSerivceMedia::class.java)
            intent.putExtra("action_music", SongSerivceMedia.ACTION_CHANGE_MUSIC)
            context.startService(intent)
            notifyDataSetChanged()
        }
    }


}