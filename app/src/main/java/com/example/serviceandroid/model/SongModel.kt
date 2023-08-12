package com.example.serviceandroid.model

import java.io.Serializable

data class SongModel(
    val nameSong: String?,
    val nameSingle: String?,
    val image: String,
    val mp3: String?,
    val nameCategory: String?
): Serializable{
    constructor() : this("", "", "", "", "")
}
