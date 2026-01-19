package com.example.copycats.obj

data class Location(
    val id: Int,
    val info: String,
    val longitude: Double,
    val latitude: Double,
    val locationOutlineFk: Int?
)