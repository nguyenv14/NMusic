package com.example.serviceandroid.presenter.CreateSong

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.example.serviceandroid.Untils
import com.example.serviceandroid.model.Category
import com.example.serviceandroid.model.SongModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage

class CreateSongPresenter(val context: Context, val createSongInterface: CreateSongInterface) {
    val db = Firebase.firestore
    var storageReference = FirebaseStorage.getInstance().reference;
    public fun getDataListCategory(){
        val list: ArrayList<String> = ArrayList<String>();
        db.collection("category").get().addOnSuccessListener { result ->
            for(document in result){
                val category = document.toObject(Category::class.java)
                list.add(category.nameCategory!!)
            }
            Toast.makeText(context, list.size.toString(), Toast.LENGTH_SHORT).show()
            createSongInterface.getDataListCategorySuccess(list)
        }.addOnFailureListener {
            Log.i("Nguyên", "Error getting documents.", it)
        }
    }

    public fun saveSong(uriMp3: Uri, uriImage: Uri, nameSong: String, nameSingle: String, nameCategory: String){
        var storageMp3 = storageReference.child("audio/${nameSong}.mp3")
        storageMp3.putFile(uriMp3).addOnSuccessListener {
            it.storage.downloadUrl.addOnSuccessListener {
                val uriMp3Down = it.toString();
                var storageImage = storageReference.child("image/${System.currentTimeMillis()}.png")
                storageImage.putFile(uriImage).addOnSuccessListener {
                    it.storage.downloadUrl.addOnSuccessListener {
                        val uriImagedown = it.toString();
                        val song = hashMapOf(
                            "nameSong" to nameSong,
                            "nameSingle" to nameSingle,
                            "image" to uriImagedown,
                            "mp3" to uriMp3Down,
                            "nameCategory" to nameCategory
                            )

                        db.collection("songs").add(song).addOnSuccessListener {
                            createSongInterface.saveSongSuccess()
                        }.addOnFailureListener {
                            Log.d("Nguyên", it.message.toString())
                            createSongInterface.saveSongError()
                        }
                    }.addOnFailureListener {
                        Log.d("Nguyên", it.message.toString())

                        createSongInterface.saveSongError()

                    }
                }
            }
        }.addOnFailureListener {
            Log.d("Nguyên", it.message.toString())
            createSongInterface.saveSongError()

        }
    }

}