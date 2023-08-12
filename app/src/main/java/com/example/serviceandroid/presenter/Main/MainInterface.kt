package com.example.serviceandroid.presenter.Main

import com.example.serviceandroid.model.SongModel

interface MainInterface {

    fun getListSongSuccess(listSong: List<SongModel>)
    fun getListSongError();
}