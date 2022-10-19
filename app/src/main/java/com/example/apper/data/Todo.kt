package com.example.apper.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Todo(val title:String, val description:String?, val isDone:Boolean, val date: String?, val time: String?, val alarm: Boolean, @PrimaryKey val id:Int? = null)
