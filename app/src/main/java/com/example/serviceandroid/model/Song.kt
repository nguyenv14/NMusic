package com.example.serviceandroid.model

import java.io.Serializable

data class Song(
    val title: String? = null,
    val single: String? = null,
    val image: Int? = null,
    val resource: Int? = null
): Serializable