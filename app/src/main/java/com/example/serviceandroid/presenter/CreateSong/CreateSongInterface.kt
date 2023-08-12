package com.example.serviceandroid.presenter.CreateSong


interface CreateSongInterface {

    public fun getDataListCategorySuccess(list: List<String>)
    public fun saveSongSuccess();
    fun saveSongError();
}