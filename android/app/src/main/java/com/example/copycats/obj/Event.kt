package com.example.copycats.obj

data class Event(
    val id: Int,
    val user_fk: Int,
    val title: String,
    val description: String,
    val start_date: String,
    val end_date: String,
    val location_fk: Int?,
    val public: Boolean,
    val tag: String?,
    val num_people: Int?
)
