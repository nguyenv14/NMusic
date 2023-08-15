package com.example.serviceandroid.presenter.Main

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.serviceandroid.Untils
import com.example.serviceandroid.model.SongModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

class MainPresenter(val context: Context, val mainInterface: MainInterface) {
    var db = Firebase.firestore
    fun getDataMusicList(){
        var listSong: ArrayList<SongModel> = ArrayList()
        var dbSong = db.collection("songs")
        dbSong.addSnapshotListener { value, error ->
            if(error != null){
                Log.w("Nguyen", "listen:error", error)
                Toast.makeText(context, "Error:" + error, Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
                mainInterface.getListSongError()
            }
            listSong.clear()
            for(item in value!!){
                val songModel = item.toObject<SongModel>(SongModel::class.java)
                listSong.add(songModel)
            }
            Untils.listSong = listSong
            Untils.musicCount = Untils.listSong.size
            mainInterface.getListSongSuccess(listSong);
        }
//        db.collection("songs").get().addOnSuccessListener {
//            for(item in it){
//                val songModel = item.toObject<SongModel>(SongModel::class.java)
//                listSong.add(songModel)
//            }
//            mainInterface.getListSongSuccess(listSong);
//        }.addOnFailureListener {
//            mainInterface.getListSongError()
//        }
    }
}